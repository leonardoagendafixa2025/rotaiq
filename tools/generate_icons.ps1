Add-Type -AssemblyName System.Drawing

function Generate-AppIcon([int]$size, [string]$outputPath, [bool]$isRound = $false) {
    $bmp = New-Object System.Drawing.Bitmap $size, $size, ([System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $g.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::AntiAliasGridFit
    $g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $g.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
    
    # 1. Fundo 100% Transparente (Sem Fundo Preto)
    $g.Clear([System.Drawing.Color]::Transparent)

    # Dimensões
    $margin = [float]($size * 0.04)
    $boxSize = [float]($size - (2 * $margin))

    # Path (Squircle ou Círculo)
    $path = New-Object System.Drawing.Drawing2D.GraphicsPath
    if ($isRound) {
        $path.AddEllipse($margin, $margin, $boxSize, $boxSize)
    } else {
        $radius = [float]($boxSize * 0.24)
        $d = [float]($radius * 2)
        $path.AddArc($margin, $margin, $d, $d, 180, 90)
        $path.AddArc(($margin + $boxSize - $d), $margin, $d, $d, 270, 90)
        $path.AddArc(($margin + $boxSize - $d), ($margin + $boxSize - $d), $d, $d, 0, 90)
        $path.AddArc($margin, ($margin + $boxSize - $d), $d, $d, 90, 90)
        $path.CloseFigure()
    }

    # 2. Cor Oficial ROTA IQ: Laranja Vibrante Unificado (#FF8200 -> #E65100)
    $p1 = New-Object System.Drawing.PointF $margin, $margin
    $p2 = New-Object System.Drawing.PointF ($margin + $boxSize), ($margin + $boxSize)
    $cTop = [System.Drawing.Color]::FromArgb(255, 130, 0)
    $cBottom = [System.Drawing.Color]::FromArgb(230, 81, 0)
    $gradBrush = New-Object System.Drawing.Drawing2D.LinearGradientBrush $p1, $p2, $cTop, $cBottom
    $g.FillPath($gradBrush, $path)

    # Borda sutil suave
    $borderPen = New-Object System.Drawing.Pen ([System.Drawing.Color]::FromArgb(40, 255, 255, 255)), [Math]::Max(1.0, [float]($size * 0.015))
    $g.DrawPath($borderPen, $path)

    # 3. Letras 'IQ' Centralizadas em Preto Grafite (#08080A)
    $fontSize = [float]($boxSize * 0.46)
    try {
        $font = New-Object System.Drawing.Font 'Arial Black', $fontSize, [System.Drawing.FontStyle]::Bold, [System.Drawing.GraphicsUnit]::Pixel
    } catch {
        $font = New-Object System.Drawing.Font 'Arial', $fontSize, [System.Drawing.FontStyle]::Bold, [System.Drawing.GraphicsUnit]::Pixel
    }

    $textBrush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(8, 8, 10))
    $sf = New-Object System.Drawing.StringFormat
    $sf.Alignment = [System.Drawing.StringAlignment]::Center
    $sf.LineAlignment = [System.Drawing.StringAlignment]::Center
    
    $textRect = New-Object System.Drawing.RectangleF $margin, ($margin + ($boxSize * 0.035)), $boxSize, $boxSize
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
Generate-AppIcon 192 "app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" $false
Generate-AppIcon 144 "app/src/main/res/mipmap-xxhdpi/ic_launcher.png" $false
Generate-AppIcon 96  "app/src/main/res/mipmap-xhdpi/ic_launcher.png" $false
Generate-AppIcon 72  "app/src/main/res/mipmap-hdpi/ic_launcher.png" $false
Generate-AppIcon 48  "app/src/main/res/mipmap-mdpi/ic_launcher.png" $false

Generate-AppIcon 192 "app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png" $true
Generate-AppIcon 144 "app/src/main/res/mipmap-xxhdpi/ic_launcher_round.png" $true
Generate-AppIcon 96  "app/src/main/res/mipmap-xhdpi/ic_launcher_round.png" $true
Generate-AppIcon 72  "app/src/main/res/mipmap-hdpi/ic_launcher_round.png" $true
Generate-AppIcon 48  "app/src/main/res/mipmap-mdpi/ic_launcher_round.png" $true

# Drawable Bitmaps de alta resolução
Generate-AppIcon 192 "app/src/main/res/drawable/ic_launcher_bitmap.png" $false
Generate-AppIcon 192 "app/src/main/res/drawable/ic_launcher_round_bitmap.png" $true

# Web Favicons e Ícones
Generate-AppIcon 512 "public/favicon.png" $false
Generate-AppIcon 512 "public/icon-512.png" $false
Generate-AppIcon 192 "public/icon-192.png" $false

Generate-AppIcon 512 "web/favicon.png" $false
Generate-AppIcon 512 "web/icon-512.png" $false
Generate-AppIcon 192 "web/icon-192.png" $false

Generate-AppIcon 512 "backend/public/favicon.png" $false
Generate-AppIcon 512 "backend/public/icon-512.png" $false
Generate-AppIcon 192 "backend/public/icon-192.png" $false

Generate-AppIcon 512 "favicon.png" $false
Generate-AppIcon 512 "icon-512.png" $false
Generate-AppIcon 192 "icon-192.png" $false
Generate-AppIcon 512 "tools/preview_icon.png" $false

Write-Host "Todos os icones e favicons laranja gerados com sucesso!"
