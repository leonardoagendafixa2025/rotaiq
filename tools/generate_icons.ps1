Add-Type -AssemblyName System.Drawing

function Generate-AppIcon([int]$size, [string]$outputPath) {
    $bmp = New-Object System.Drawing.Bitmap $size, $size
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $g.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::AntiAliasGridFit
    $g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    
    # Fundo escuro sutil
    $g.Clear([System.Drawing.Color]::FromArgb(10, 14, 23))

    # Dimensões proporcionais do squircle
    $margin = [int]($size * 0.08)
    $boxSize = $size - (2 * $margin)
    $radius = [int]($boxSize * 0.26)
    $d = $radius * 2

    # Squircle Path
    $path = New-Object System.Drawing.Drawing2D.GraphicsPath
    $path.AddArc($margin, $margin, $d, $d, 180, 90)
    $path.AddArc($margin + $boxSize - $d, $margin, $d, $d, 270, 90)
    $path.AddArc($margin + $boxSize - $d, $margin + $boxSize - $d, $d, $d, 0, 90)
    $path.AddArc($margin, $margin + $boxSize - $d, $d, $d, 90, 90)
    $path.CloseFigure()

    # Gradiente ciano vibrante
    $p1 = New-Object System.Drawing.Point $margin, $margin
    $p2 = New-Object System.Drawing.Point ($margin + $boxSize), ($margin + $boxSize)
    $cTop = [System.Drawing.Color]::FromArgb(0, 229, 255)
    $cBottom = [System.Drawing.Color]::FromArgb(0, 130, 210)
    $gradBrush = New-Object System.Drawing.Drawing2D.LinearGradientBrush $p1, $p2, $cTop, $cBottom
    $g.FillPath($gradBrush, $path)

    # Borda sutil interna mais clara
    $borderPen = New-Object System.Drawing.Pen ([System.Drawing.Color]::FromArgb(80, 255, 255, 255)), [float]($size * 0.015)
    $g.DrawPath($borderPen, $path)

    # Desenhar 'IQ' centralizado e super bold
    $fontSize = [float]($boxSize * 0.46)
    $fontFamily = New-Object System.Drawing.FontFamily 'Arial'
    # Tenta usar Arial Black ou Arial Bold
    try {
        $font = New-Object System.Drawing.Font 'Arial Black', $fontSize, [System.Drawing.FontStyle]::Bold, [System.Drawing.GraphicsUnit]::Pixel
    } catch {
        $font = New-Object System.Drawing.Font 'Arial', $fontSize, [System.Drawing.FontStyle]::Bold, [System.Drawing.GraphicsUnit]::Pixel
    }

    $textBrush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(10, 14, 23))
    $sf = New-Object System.Drawing.StringFormat
    $sf.Alignment = [System.Drawing.StringAlignment]::Center
    $sf.LineAlignment = [System.Drawing.StringAlignment]::Center
    
    $textRect = New-Object System.Drawing.RectangleF $margin, ($margin + ($size * 0.02)), $boxSize, $boxSize
    $g.DrawString('IQ', $font, $textBrush, $textRect, $sf)

    # Assegurar diretório de destino
    $dir = [System.IO.Path]::GetDirectoryName($outputPath)
    if (-not (Test-Path $dir)) {
        New-Item -ItemType Directory -Force -Path $dir | Out-Null
    }

    $bmp.Save($outputPath, [System.Drawing.Imaging.ImageFormat]::Png)
    
    $borderPen.Dispose()
    $gradBrush.Dispose()
    $textBrush.Dispose()
    $font.Dispose()
    $path.Dispose()
    $g.Dispose()
    $bmp.Dispose()
    
    Write-Host "Gerado: $outputPath ($size x $size)"
}

# Gerar todas as densidades Android
Generate-AppIcon 512 "web/icon-512.png"
Generate-AppIcon 192 "app/src/main/res/mipmap-xxxhdpi/ic_launcher.png"
Generate-AppIcon 144 "app/src/main/res/mipmap-xxhdpi/ic_launcher.png"
Generate-AppIcon 96  "app/src/main/res/mipmap-xhdpi/ic_launcher.png"
Generate-AppIcon 72  "app/src/main/res/mipmap-hdpi/ic_launcher.png"
Generate-AppIcon 48  "app/src/main/res/mipmap-mdpi/ic_launcher.png"

# Round icons
Generate-AppIcon 192 "app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png"
Generate-AppIcon 144 "app/src/main/res/mipmap-xxhdpi/ic_launcher_round.png"
Generate-AppIcon 96  "app/src/main/res/mipmap-xhdpi/ic_launcher_round.png"
Generate-AppIcon 72  "app/src/main/res/mipmap-hdpi/ic_launcher_round.png"
Generate-AppIcon 48  "app/src/main/res/mipmap-mdpi/ic_launcher_round.png"
