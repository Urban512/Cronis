$ErrorActionPreference = 'Stop'
$iconsDir = Join-Path $PSScriptRoot '..\src\main\resources\assets\cronis\icons'
New-Item -ItemType Directory -Force -Path $iconsDir | Out-Null

Add-Type -AssemblyName System.Drawing

function New-CronisIcon {
    param(
        [string]$Name,
        [scriptblock]$Draw
    )

    $bmp = New-Object System.Drawing.Bitmap 16, 16, ([System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $g.Clear([System.Drawing.Color]::FromArgb(0, 0, 0, 0))
    $brush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(255, 240, 243, 248))
    & $Draw $g $brush
    $brush.Dispose()
    $g.Dispose()
    $path = Join-Path $iconsDir "$Name.png"
    $bmp.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Dispose()
}

New-CronisIcon 'settings' {
    param($g, $b)
    $g.FillEllipse($b, 2, 2, 12, 12)
    $g.FillEllipse($b, 6, 6, 4, 4)
}

New-CronisIcon 'search' {
    param($g, $b)
    $pen = New-Object System.Drawing.Pen($b.Color, 1.5)
    $g.DrawEllipse($pen, 3, 3, 8, 8)
    $g.DrawLine($pen, 10, 10, 13, 13)
    $pen.Dispose()
}

New-CronisIcon 'discord' {
    param($g, $b)
    $g.FillRectangle($b, 2, 5, 12, 6)
    $g.FillRectangle($b, 4, 4, 8, 2)
}

New-CronisIcon 'github' {
    param($g, $b)
    $g.FillEllipse($b, 4, 2, 8, 8)
    $g.FillRectangle($b, 3, 8, 10, 5)
}

New-CronisIcon 'close' {
    param($g, $b)
    $pen = New-Object System.Drawing.Pen($b.Color, 1.5)
    $g.DrawLine($pen, 4, 4, 12, 12)
    $g.DrawLine($pen, 12, 4, 4, 12)
    $pen.Dispose()
}

New-CronisIcon 'music' {
    param($g, $b)
    $g.FillEllipse($b, 3, 9, 4, 4)
    $g.FillEllipse($b, 9, 7, 4, 4)
    $g.FillRectangle($b, 6, 3, 2, 8)
    $g.FillRectangle($b, 12, 3, 2, 6)
}

New-CronisIcon 'spotify' {
    param($g, $b)
    $g.FillEllipse($b, 2, 2, 12, 12)
    $dark = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(255, 20, 20, 24))
    $g.FillEllipse($dark, 5, 5, 6, 6)
    $dark.Dispose()
}

New-CronisIcon 'warning' {
    param($g, $b)
    $points = @(
        [System.Drawing.Point]::new(8, 2),
        [System.Drawing.Point]::new(14, 14),
        [System.Drawing.Point]::new(2, 14)
    )
    $g.FillPolygon($b, $points)
}

New-CronisIcon 'success' {
    param($g, $b)
    $pen = New-Object System.Drawing.Pen($b.Color, 1.5)
    $g.DrawLine($pen, 3, 8, 7, 12)
    $g.DrawLine($pen, 7, 12, 13, 4)
    $pen.Dispose()
}

New-CronisIcon 'arrow_left' {
    param($g, $b)
    $pen = New-Object System.Drawing.Pen($b.Color, 1.5)
    $g.DrawLine($pen, 9, 3, 4, 8)
    $g.DrawLine($pen, 4, 8, 9, 13)
    $pen.Dispose()
}

New-CronisIcon 'arrow_right' {
    param($g, $b)
    $pen = New-Object System.Drawing.Pen($b.Color, 1.5)
    $g.DrawLine($pen, 7, 3, 12, 8)
    $g.DrawLine($pen, 12, 8, 7, 13)
    $pen.Dispose()
}

Get-ChildItem $iconsDir | ForEach-Object { Write-Output $_.Name }
