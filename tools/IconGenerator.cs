using System;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Drawing.Imaging;
using System.Drawing.Text;
using System.IO;

public class Program {
    public static void Main(string[] args) {
        string rootDir = args.Length > 0 ? args[0] : ".";

        int[] sizes = new int[] { 48, 72, 96, 144, 192 };
        string[] dirs = new string[] {
            "app/src/main/res/mipmap-mdpi",
            "app/src/main/res/mipmap-hdpi",
            "app/src/main/res/mipmap-xhdpi",
            "app/src/main/res/mipmap-xxhdpi",
            "app/src/main/res/mipmap-xxxhdpi"
        };

        for (int i = 0; i < sizes.Length; i++) {
            int sz = sizes[i];
            string targetDir = Path.Combine(rootDir, dirs[i]);
            Directory.CreateDirectory(targetDir);

            GenerateIcon(sz, Path.Combine(targetDir, "ic_launcher.png"), false);
            GenerateIcon(sz, Path.Combine(targetDir, "ic_launcher_round.png"), true);
            Console.WriteLine("Generated " + sz + "x" + sz + " for " + dirs[i]);
        }

        // Drawable Bitmaps de alta resolução para Android
        string drawableDir = Path.Combine(rootDir, "app/src/main/res/drawable");
        Directory.CreateDirectory(drawableDir);
        GenerateIcon(192, Path.Combine(drawableDir, "ic_launcher_bitmap.png"), false);
        GenerateIcon(192, Path.Combine(drawableDir, "ic_launcher_round_bitmap.png"), true);

        // Web Favicons e Ícones em múltiplos diretórios (public, web, backend/public, root)
        string[] webDirs = new string[] {
            Path.Combine(rootDir, "public"),
            Path.Combine(rootDir, "web"),
            Path.Combine(rootDir, "backend/public"),
            rootDir
        };

        foreach (string wDir in webDirs) {
            if (Directory.Exists(wDir)) {
                GenerateIcon(512, Path.Combine(wDir, "favicon.png"), false);
                GenerateIcon(512, Path.Combine(wDir, "icon-512.png"), false);
                GenerateIcon(192, Path.Combine(wDir, "icon-192.png"), false);
                Console.WriteLine("Generated web favicon and icons in: " + wDir);
            }
        }

        Console.WriteLine("All ROTA IQ unified orange icons generated successfully!");
    }

    public static void GenerateIcon(int size, string outputPath, bool isRound) {
        using (Bitmap bmp = new Bitmap(size, size, PixelFormat.Format32bppArgb))
        using (Graphics g = Graphics.FromImage(bmp)) {
            g.SmoothingMode = SmoothingMode.AntiAlias;
            g.InterpolationMode = InterpolationMode.HighQualityBicubic;
            g.PixelOffsetMode = PixelOffsetMode.HighQuality;
            g.TextRenderingHint = TextRenderingHint.AntiAliasGridFit;

            // 1. FUNDO 100% TRANSPARENTE — SEM FUNDO PRETO
            g.Clear(Color.Transparent);

            float margin = size * 0.04f;
            float boxSize = size - (2f * margin);

            using (GraphicsPath path = new GraphicsPath()) {
                if (isRound) {
                    path.AddEllipse(margin, margin, boxSize, boxSize);
                } else {
                    float radius = boxSize * 0.24f;
                    float d = radius * 2f;
                    path.AddArc(margin, margin, d, d, 180, 90);
                    path.AddArc(margin + boxSize - d, margin, d, d, 270, 90);
                    path.AddArc(margin + boxSize - d, margin + boxSize - d, d, d, 0, 90);
                    path.AddArc(margin, margin + boxSize - d, d, d, 90, 90);
                    path.CloseFigure();
                }

                // 2. COR OFICIAL ROTA IQ: LARANJA VIBRANTE UNIFICADO (#FF7A00)
                PointF ptTop = new PointF(margin, margin);
                PointF ptBottom = new PointF(margin + boxSize, margin + boxSize);
                Color topOrange = Color.FromArgb(255, 130, 0);  // #FF8200
                Color bottomOrange = Color.FromArgb(230, 81, 0); // #E65100

                using (LinearGradientBrush orangeBrush = new LinearGradientBrush(ptTop, ptBottom, topOrange, bottomOrange)) {
                    g.FillPath(orangeBrush, path);
                }

                // Borda de acabamento suave sutil
                using (Pen borderPen = new Pen(Color.FromArgb(40, 255, 255, 255), Math.Max(1f, size * 0.015f))) {
                    g.DrawPath(borderPen, path);
                }

                // 3. LETRAS 'IQ' CENTRALIZADAS EM PRETO GRAFITE ELEGANTE (#08080A)
                float fontSize = boxSize * 0.46f;
                using (Font font = new Font("Arial Black", fontSize, FontStyle.Bold, GraphicsUnit.Pixel))
                using (Brush textBrush = new SolidBrush(Color.FromArgb(8, 8, 10))) {
                    StringFormat sf = new StringFormat();
                    sf.Alignment = StringAlignment.Center;
                    sf.LineAlignment = StringAlignment.Center;
                    RectangleF rect = new RectangleF(margin, margin + (boxSize * 0.035f), boxSize, boxSize);
                    g.DrawString("IQ", font, textBrush, rect, sf);
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
