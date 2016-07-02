#################################
##  script to create gif from mp4
#################################



#Generate a palette
ffmpeg -y -ss 30 -t 3 -i PipeCutter.mp4 -vf fps=10,scale=320:-1:flags=lanczos,palettegen palette.png

#convert to gif
ffmpeg -ss 30 -t 200 -i PipeCutter.mp4 -i palette.png -filter_complex "fps=10,scale=1024:-1:flags=lanczos[x];[x][1:v]paletteuse" -fs 10485760 PipeCutterVideo.gif

