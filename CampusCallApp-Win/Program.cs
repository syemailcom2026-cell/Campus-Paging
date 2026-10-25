using System;
using System.Collections.Generic;
using System.Drawing;
using System.IO;
using System.Media;
using System.Reflection;
using System.Text.Json;
using System.Threading.Tasks;
using System.Windows.Forms;
using Microsoft.Web.WebView2.Core;
using Microsoft.Web.WebView2.WinForms;

namespace CampusCallWin
{
    internal static class Program
    {
        [STAThread]
        private static void Main(string[] args)
        {
            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);

            // 启动参数：--room 123456 --role display（自动化测试 / 快捷进入用）
            string room = "", role = "";
            for (int i = 0; i < args.Length - 1; i++)
            {
                if (args[i] == "--room") room = args[i + 1];
                else if (args[i] == "--role") role = args[i + 1];
            }

            Application.Run(new MainForm(room, role));
        }
    }

    /// <summary>右下角呼叫小屏：无边框、置顶、持续挂起直到呼叫结束</summary>
    internal class PopupForm : Form
    {
        private readonly Label _titleLbl;
        private readonly Label _textLbl;

        public event EventHandler CloseRequested;

        // 拖动状态（无边框窗体手动拖动）
        private bool _dragging;
        private Point _dragPoint;

        public PopupForm(string title, string text)
        {
            FormBorderStyle = FormBorderStyle.None;
            ShowInTaskbar = false;
            TopMost = true;
            Width = 400;
            Height = 116;
            BackColor = Color.FromArgb(21, 29, 44);

            var closeBtn = new Button
            {
                Text = "✕",
                FlatStyle = FlatStyle.Flat,
                Width = 28,
                Height = 24,
                Left = Width - 34,
                Top = 5,
                Anchor = AnchorStyles.Top | AnchorStyles.Right,
                BackColor = Color.Transparent,
                ForeColor = Color.FromArgb(139, 147, 166),
                Cursor = Cursors.Hand
            };
            closeBtn.FlatAppearance.BorderSize = 0;
            closeBtn.FlatAppearance.MouseOverBackColor = Color.FromArgb(34, 46, 66);
            closeBtn.Click += (s, e) => { CloseRequested?.Invoke(this, EventArgs.Empty); };

            _titleLbl = new Label
            {
                Text = title,
                ForeColor = Color.FromArgb(56, 189, 248),
                Font = new Font("Microsoft YaHei UI", 10.5f, FontStyle.Bold),
                AutoSize = true,
                Location = new Point(14, 10)
            };

            _textLbl = new Label
            {
                Text = text,
                ForeColor = Color.FromArgb(232, 238, 252),
                Font = new Font("Microsoft YaHei UI", 10f),
                AutoSize = true,
                MaximumSize = new Size(370, 0),
                Location = new Point(14, 38)
            };

            Controls.Add(closeBtn);
            Controls.Add(_titleLbl);
            Controls.Add(_textLbl);

            // 整窗可拖动（点击空白/标题/正文拖动小屏，✕ 按钮除外）
            MouseDown += OnDragStart;
            MouseMove += OnDragMove;
            MouseUp += OnDragEnd;
            _titleLbl.MouseDown += OnDragStart;
            _titleLbl.MouseMove += OnDragMove;
            _titleLbl.MouseUp += OnDragEnd;
            _textLbl.MouseDown += OnDragStart;
            _textLbl.MouseMove += OnDragMove;
            _textLbl.MouseUp += OnDragEnd;
        }

        private void OnDragStart(object sender, MouseEventArgs e)
        {
            if (e.Button == MouseButtons.Left)
            {
                _dragging = true;
                _dragPoint = e.Location;
            }
        }

        private void OnDragMove(object sender, MouseEventArgs e)
        {
            if (_dragging)
            {
                Point screen = PointToScreen(e.Location);
                Location = new Point(screen.X - _dragPoint.X, screen.Y - _dragPoint.Y);
            }
        }

        private void OnDragEnd(object sender, MouseEventArgs e)
        {
            _dragging = false;
        }

        public void UpdateText(string title, string text)
        {
            _titleLbl.Text = title;
            _textLbl.Text = text;
        }
    }

    internal class MainForm : Form
    {
        private readonly WebView2 _webView;
        private readonly string _appDir;
        private readonly string _autoRoom;
        private readonly string _autoRole;
        private readonly Dictionary<string, PopupForm> _popups = new Dictionary<string, PopupForm>();
        private NotifyIcon _tray;
        private bool _exiting;
        private string _role = "";
        private string _popStyle = "small";
        private System.Threading.CancellationTokenSource _hideCts;

        public MainForm(string autoRoom, string autoRole)
        {
            _autoRoom = autoRoom;
            _autoRole = autoRole;

            Text = "校园传呼";
            ClientSize = new Size(1200, 800);
            MinimumSize = new Size(480, 640);
            StartPosition = FormStartPosition.CenterScreen;

            try
            {
                Icon = Icon.ExtractAssociatedIcon(Application.ExecutablePath);
            }
            catch
            {
                // 图标获取失败不影响功能
            }

            // 把内嵌的页面释放到临时目录，再通过虚拟域名加载
            _appDir = Path.Combine(Path.GetTempPath(), "CampusCallAppWin");
            Directory.CreateDirectory(_appDir);
            ExtractHtml();

            _webView = new WebView2 { Dock = DockStyle.Fill };
            Controls.Add(_webView);
            Load += OnLoad;
            SetupTray();
        }

        private void SetupTray()
        {
            _tray = new NotifyIcon
            {
                Icon = Icon,
                Text = "校园传呼",
                Visible = true
            };

            var menu = new ContextMenuStrip();
            menu.Items.Add("打开主界面", null, (s, e) => ShowWindow());
            menu.Items.Add("退出校园传呼", null, (s, e) => QuitApp());
            _tray.ContextMenuStrip = menu;
            _tray.DoubleClick += (s, e) => ShowWindow();
        }

        private void ShowWindow()
        {
            if (InvokeRequired)
            {
                BeginInvoke(new Action(ShowWindow));
                return;
            }
            Show();
            WindowState = FormWindowState.Normal;
            Activate();
        }

        /// <summary>收到广播时强制把主窗口带到屏幕前台（后台运行也能弹出大屏）</summary>
        private void ForceToFront()
        {
            if (InvokeRequired)
            {
                BeginInvoke(new Action(ForceToFront));
                return;
            }
            if (_exiting) return;
            Show();
            // 1.2.8：强制弹出大屏显示播报时，默认窗口最大化
            WindowState = FormWindowState.Maximized;
            // 先置顶一次再取消，绕过 Windows 对后台程序抢焦点的限制
            TopMost = true;
            Activate();
            TopMost = false;
        }

        /// <summary>播报结束：大屏主窗口退回后台（最小化到托盘继续运行）</summary>
        private void HideToTray()
        {
            if (InvokeRequired)
            {
                BeginInvoke(new Action(HideToTray));
                return;
            }
            if (_exiting) return;
            Hide();
        }

        /// <summary>1.2.9：播报结束后先停顿 5 秒，再让大屏退回后台（让观看者看清播报内容）</summary>
        private void ScheduleHide()
        {
            CancelHide();
            var cts = new System.Threading.CancellationTokenSource();
            _hideCts = cts;
            Task.Run(async () =>
            {
                try
                {
                    await Task.Delay(5000, cts.Token);
                }
                catch (TaskCanceledException)
                {
                    return;
                }
                if (cts.IsCancellationRequested) return;
                HideToTray();
            });
        }

        /// <summary>取消待执行的延迟退回后台（新广播到达时重置计时）</summary>
        private void CancelHide()
        {
            var c = _hideCts;
            _hideCts = null;
            if (c != null)
            {
                try { c.Cancel(); c.Dispose(); } catch { }
            }
        }

        private void QuitApp()
        {
            _exiting = true;
            CancelHide();
            if (_tray != null)
            {
                _tray.Visible = false;
                _tray.Dispose();
                _tray = null;
            }
            foreach (PopupForm f in _popups.Values)
            {
                try { f.Close(); f.Dispose(); } catch { }
            }
            _popups.Clear();
            Close();
            Application.Exit();
        }

        protected override void OnFormClosing(FormClosingEventArgs e)
        {
            if (_exiting) return;
            // 后台运行：点关闭按钮时隐藏到系统托盘，不退出程序
            e.Cancel = true;
            Hide();
            base.OnFormClosing(e);
        }

                private void ExtractHtml()
        {
            string target = Path.Combine(_appDir, "index.html");
            using (Stream s = Assembly.GetExecutingAssembly()
                       .GetManifestResourceStream("CampusCallWin.index.html"))
            using (FileStream fs = File.Create(target))
            {
                if (s == null)
                {
                    throw new InvalidOperationException("内嵌页面资源缺失");
                }

                s.CopyTo(fs);
            }

            // 页面引用的 MQTT 库同样从嵌入资源提取（每次启动覆盖，避免被清理后丢失）
            string mqttTarget = Path.Combine(_appDir, "mqtt.min.js");
            using (Stream ms = Assembly.GetExecutingAssembly()
                       .GetManifestResourceStream("CampusCallWin.mqtt.min.js"))
            using (FileStream fs = File.Create(mqttTarget))
            {
                if (ms != null)
                {
                    ms.CopyTo(fs);
                }
            }
        }

        private async void OnLoad(object sender, EventArgs e)
        {
            try
            {
                await _webView.EnsureCoreWebView2Async(null);
                _webView.CoreWebView2.SetVirtualHostNameToFolderMapping(
                    "app.local", _appDir, CoreWebView2HostResourceAccessKind.DenyCors);
                _webView.CoreWebView2.WebMessageReceived += OnWebMessage;
                _webView.CoreWebView2.Navigate("https://app.local/index.html");

                // 启动参数：等页面就绪后自动进入指定角色与房间
                if (!string.IsNullOrEmpty(_autoRoom) && !string.IsNullOrEmpty(_autoRole))
                {
                    await WaitAndAutoEnter();
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show("启动失败：" + ex.Message, "校园传呼",
                    MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }


        private async Task WaitAndAutoEnter()
        {
            for (int i = 0; i < 240; i++)
            {
                try
                {
                    string ready = await _webView.CoreWebView2.ExecuteScriptAsync(
                        "typeof window.__autoEnter === 'function' ? 1 : 0");
                    if (ready == "1")
                    {
                        await _webView.CoreWebView2.ExecuteScriptAsync(
                            "window.__autoEnter('" + _autoRoom + "','" + _autoRole + "');");

                        return;
                    }
                }
                catch
                {
                    // 页面尚未就绪，继续等待
                }
                await Task.Delay(250);
            }
        }

        private void OnWebMessage(object sender, CoreWebView2WebMessageReceivedEventArgs e)
        {
            try
            {
                // JS 端 postMessage 的字符串会被 JSON 编码，先还原
                string json = JsonSerializer.Deserialize<string>(e.WebMessageAsJson);
                if (string.IsNullOrEmpty(json)) return;

                using (JsonDocument doc = JsonDocument.Parse(json))
                {
                    JsonElement root = doc.RootElement;
                    if (!root.TryGetProperty("type", out JsonElement t))
                    {
                        return;
                    }

                    string type = t.GetString() ?? "";
                    string id = GetStr(root, "id");
                    string text = GetStr(root, "text");

                    if (type == "role")
                    {
                        // 页面进入房间后上报角色，决定大屏端/呼叫端的弹窗行为
                        _role = GetStr(root, "role");
                        // 1.4.1：弹出方式（小窗 / 大屏）由大屏端设置随 role 一并上报
                        string ps = GetStr(root, "popstyle");
                        if (!string.IsNullOrEmpty(ps)) _popStyle = ps;
                    }
                    else if (type == "popstyle")
                    {
                        // 大屏端设置中切换弹出方式
                        string ps = GetStr(root, "value");
                        if (!string.IsNullOrEmpty(ps)) _popStyle = ps;
                    }
                    else if (type == "closeDisplay")
                    {
                        // 大屏手动收起窗口：立即退回后台，取消待执行的延迟退回
                        CancelHide();
                        HideToTray();
                    }
                    else if (type == "call")
                    {
                        if (_role == "display")
                        {
                            CancelHide(); // 新广播到达：重置可能正在进行的延迟退回
                            if (_popStyle == "big")
                            {
                                // 1.4.1 大屏模式：强制全屏弹出显示播报（不弹小窗），播报结束由 done 处理退回后台
                                ForceToFront();
                                try { SystemSounds.Exclamation.Play(); } catch { }
                            }
                            else
                            {
                                // 小窗模式（默认）：右下角通知弹窗，提示声，挂到呼叫结束
                                try { SystemSounds.Exclamation.Play(); } catch { }
                                ShowPopup(id, "校园传呼 · 收到广播",
                                    string.IsNullOrEmpty(text) ? "收到一条呼叫" : text, true);
                            }
                        }
                        else
                        {
                            ShowPopup(id, "校园传呼 · 收到广播",
                                string.IsNullOrEmpty(text) ? "收到一条呼叫" : text);
                        }
                    }
                    else if (type == "openUrl")
                    {
                        // 1.4.2：网页版入口——用系统默认浏览器打开网页版
                        string u = GetStr(root, "url");
                        if (!string.IsNullOrEmpty(u))
                        {
                            try { System.Diagnostics.Process.Start(new System.Diagnostics.ProcessStartInfo(u) { UseShellExecute = true }); } catch { }
                        }
                    }
                    else if (type == "sent")
                    {
                        // 呼叫端：呼叫已发出 → 顶部小屏（可拖动），挂到呼叫结束
                        ShowPopup(id, "校园传呼 · 呼叫已发出",
                            string.IsNullOrEmpty(text) ? "呼叫已发出" : text);
                    }
                    else if (type == "done")
                    {
                        ClosePopup(id);
                        // 1.4.3：大屏退回后台由 HTML 按「大屏延迟关闭」配置（默认 1 秒）延迟后发 closeDisplay 控制，不再固定 5 秒
                    }
                    else if (type == "reply")
                    {
                        // 接收端回复（知道了 / 文字回复）→ 呼叫端顶部小屏显示
                        string rtext = GetStr(root, "text");
                        if (string.IsNullOrEmpty(rtext)) rtext = "（无内容）";
                        ShowPopup(GetStr(root, "id"), "校园传呼 · 收到回复", rtext);
                    }
                    else if (type == "msg")
                    {
                        // 1.4.4：接收端呼叫结束后发送的消息 → 呼叫端顶部小屏显示
                        string mtext = GetStr(root, "text");
                        if (string.IsNullOrEmpty(mtext)) mtext = "（无内容）";
                        ShowPopup(GetStr(root, "id"), "校园传呼 · 收到大屏消息", mtext);
                    }
                }
            }
            catch
            {
                // 消息格式异常时忽略
            }
        }

        private void ShowPopup(string id, string title, string text, bool rightBottom = false)
        {
            if (string.IsNullOrEmpty(id))
            {
                id = "x" + Guid.NewGuid().ToString("N").Substring(0, 8);
            }

            if (_popups.TryGetValue(id, out PopupForm existing))
            {
                existing.UpdateText(title, text);
                return;
            }

            var f = new PopupForm(title, text);
            f.StartPosition = FormStartPosition.Manual; // 禁用系统级联定位，位置严格按 Location
            f.CloseRequested += (s, ev) => ClosePopup(id);

            var wa = Screen.PrimaryScreen.WorkingArea;
            int idx = _popups.Count;
            int x, y;
            if (rightBottom)
            {
                // 1.3.1：右下角通知弹窗（类似通知位置），多条依次向上排列
                x = wa.Right - f.Width - 16;
                y = wa.Bottom - f.Height - 16 - idx * (f.Height + 8);
                if (y < wa.Top + 12) y = wa.Bottom - f.Height - 16;
            }
            else
            {
                // 呼叫端小屏：显示在屏幕正上方（顶部居中），多条依次向下排列
                x = wa.Left + (wa.Width - f.Width) / 2;
                y = wa.Top + 12 + idx * (f.Height + 8);
                if (y + f.Height > wa.Bottom) y = wa.Top + 12;
            }
            f.Location = new Point(x, y);

            _popups[id] = f;
            f.Show();
            f.Location = new Point(x, y); // Show 后再次确认位置（防止系统级联定位覆盖）

            // 提示声
            try { SystemSounds.Exclamation.Play(); } catch { }

            // 日志：用于验证小屏链路（%TEMP%\CampusCallAppWin\notify.log）
            LogNotify("[popup open] " + title + " " + text);
        }

        private void ClosePopup(string id)
        {
            if (_popups.TryGetValue(id, out PopupForm f))
            {
                _popups.Remove(id);
                try { f.Close(); f.Dispose(); } catch { }
                LogNotify("[popup close] id=" + id);
            }
        }

        private void LogNotify(string line)
        {
            try
            {
                File.AppendAllText(Path.Combine(_appDir, "notify.log"),
                    DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss") + " " + line + "\r\n");
            }
            catch
            {
                // 日志写入失败不影响功能
            }
        }

        private static string GetStr(JsonElement root, string key)
        {
            if (root.TryGetProperty(key, out JsonElement v) && v.ValueKind == JsonValueKind.String)
            {
                return v.GetString() ?? "";
            }
            return "";
        }
    }
}
