using System;
using System.Drawing;
using System.IO;
using System.Reflection;
using System.Windows.Forms;
using Microsoft.Web.WebView2.Core;
using Microsoft.Web.WebView2.WinForms;

namespace CampusCallWin
{
    internal static class Program
    {
        [STAThread]
        private static void Main()
        {
            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);
            Application.Run(new MainForm());
        }
    }

    internal class MainForm : Form
    {
        private readonly WebView2 _webView;
        private readonly string _appDir;

        public MainForm()
        {
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
                // 窗口图标获取失败不影响功能
            }

            // 把内嵌的页面释放到临时目录，再通过虚拟域名加载，
            // 保证 localStorage 与 WebSocket 等现代能力正常。
            _appDir = Path.Combine(Path.GetTempPath(), "CampusCallAppWin");
            Directory.CreateDirectory(_appDir);
            ExtractHtml();

            _webView = new WebView2 { Dock = DockStyle.Fill };
            Controls.Add(_webView);
            Load += OnLoad;
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
        }

        private async void OnLoad(object sender, EventArgs e)
        {
            try
            {
                await _webView.EnsureCoreWebView2Async(null);
                _webView.CoreWebView2.SetVirtualHostNameToFolderMapping(
                    "app.local", _appDir, CoreWebView2HostResourceAccessKind.DenyCors);
                _webView.CoreWebView2.Navigate("https://app.local/index.html");
            }
            catch (Exception ex)
            {
                MessageBox.Show("启动失败：" + ex.Message, "校园呼叫播报系统",
                    MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }

        protected override void OnFormClosing(FormClosingEventArgs e)
        {
            _webView?.Dispose();
            base.OnFormClosing(e);
        }
    }
}
