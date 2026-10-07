using System;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Drawing.Imaging;
using System.Drawing.Text;
using System.IO;

public class Program {
    public static void Main(string[] args) {
        string rootDir = args.Length > 0 ? args[0] : ".";

        int[] sizes = new int[] { 48, 72, 96, 144, 192, 512 };
        string[] dirs = new string[] {
            "app/src/main/res/mipmap-mdpi",
            "app/src/main/res/mipmap-hdpi",
            "app/src/main/res/mipmap-xhdpi",
            "app/src/main/res/mipmap-xxhdpi",
            "app/src/main/res/mipmap-xxxhdpi",
            "web"
        };

        for (int i = 0; i < sizes.Length; i++) {
            int sz = sizes[i];
            string targetDir = Path.Combine(rootDir, dirs[i]);
            Directory.CreateDirectory(targetDir);

            if (sz == 512) {
                GenerateIcon(sz, Path.Combine(targetDir, "icon-512.png"), false);
                GenerateIcon(sz, Path.Combine(targetDir, "favicon.png"), false);
                Console.WriteLine("Generated 512x512 web icons");
            } else {
                GenerateIcon(sz, Path.Combine(targetDir, "ic_launcher.png"), false);
                GenerateIcon(sz, Path.Combine(targetDir, "ic_launcher_round.png"), true);
                Console.WriteLine("Generated " + sz + "x" + sz + " for " + dirs[i]);
            }
        }

        // Também gerar cópia de alta resolução para drawable/
        string drawableDir = Path.Combine(rootDir, "app/src/main/res/drawable");
        Directory.CreateDirectory(drawableDir);
        GenerateIcon(192, Path.Combine(drawableDir, "ic_launcher_bitmap.png"), false);
        GenerateIcon(192, Path.Combine(drawableDir, "ic_launcher_round_bitmap.png"), true);

        Console.WriteLine("All Android launcher icons generated successfully!");
    }

    public static void GenerateIcon(int size, string outputPath, bool isRound) {
        using (Bitmap bmp = new Bitmap(size, size, PixelFormat.Format32bppArgb))
        using (Graphics g = Graphics.FromImage(bmp)) {
            g.SmoothingMode = SmoothingMode.AntiAlias;
            g.InterpolationMode = InterpolationMode.HighQualityBicubic;
            g.PixelOffsetMode = PixelOffsetMode.HighQuality;
            g.TextRenderingHint = TextRenderingHint.AntiAliasGridFit;

            // Fundo escuro sutil
            g.Clear(Color.FromArgb(10, 16, 25));

            float margin = size * 0.06f;
            float boxSize = size - (2f * margin);

            using (GraphicsPath squirclePath = new GraphicsPath()) {
                if (isRound) {
                    squirclePath.AddEllipse(margin, margin, boxSize, boxSize);
                } else {
                    float radius = boxSize * 0.28f;
                    float d = radius * 2f;
                    squirclePath.AddArc(margin, margin, d, d, 180, 90);
                    squirclePath.AddArc(margin + boxSize - d, margin, d, d, 270, 90);
                    squirclePath.AddArc(margin + boxSize - d, margin + boxSize - d, d, d, 0, 90);
                    squirclePath.AddArc(margin, margin + boxSize - d, d, d, 90, 90);
                    squirclePath.CloseFigure();
                }

                // Brilho externo sutil (glow ciano)
                using (Pen outerGlow = new Pen(Color.FromArgb(45, 0, 210, 255), Math.Max(1f, size * 0.035f))) {
                    g.DrawPath(outerGlow, squirclePath);
                }

                // Gradiente Ciano Elétrico oficial da marca
                PointF ptTop = new PointF(size * 0.5f, margin);
                PointF ptBottom = new PointF(size * 0.5f, margin + boxSize);
                Color topCyan = Color.FromArgb(0, 215, 255);
                Color bottomCyan = Color.FromArgb(0, 145, 220);

                using (LinearGradientBrush cyanBrush = new LinearGradientBrush(ptTop, ptBottom, topCyan, bottomCyan)) {
                    g.FillPath(cyanBrush, squirclePath);
                }

                // Borda interna / bevel suave
                using (Pen innerSheen = new Pen(Color.FromArgb(60, 255, 255, 255), Math.Max(1f, size * 0.012f))) {
                    g.DrawPath(innerSheen, squirclePath);
                }

                // Letras 'IQ' perfeitamente centralizadas
                float fontSize = boxSize * 0.47f;
                using (Font font = new Font("Arial Black", fontSize, FontStyle.Bold, GraphicsUnit.Pixel))
                using (Brush brush = new SolidBrush(Color.FromArgb(6, 10, 16))) {
                    StringFormat sf = new StringFormat();
                    sf.Alignment = StringAlignment.Center;
                    sf.LineAlignment = StringAlignment.Center;
                    RectangleF rect = new RectangleF(margin, margin + (boxSize * 0.035f), boxSize, boxSize);
                    g.DrawString("IQ", font, brush, rect, sf);
                }
            }

            string dir = Path.GetDirectoryName(outputPath);
            if (!string.IsNullOrEmpty(dir) && !Directory.Exists(dir)) {
                Directory.CreateDirectory(dir);
            }
            bmp.Save(outputPath, ImageFormat.Png);
        }
    }
}
