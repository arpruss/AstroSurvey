mkdir res/drawable/ldpi
mkdir res/drawable/mdpi
mkdir res/drawable/hdpi
mkdir res/drawable/xhdpi
convert icon512.png -resize 36x36 res/drawable-ldpi/icon.png
convert icon512.png -resize 48x48 res/drawable-mdpi/icon.png
convert icon512.png -resize 72x72 res/drawable-hdpi/icon.png
convert icon512.png -resize 96x96 res/drawable-xhdpi/icon.png
