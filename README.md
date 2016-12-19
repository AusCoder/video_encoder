## Video Encoder

This is a solution to a video encoder coding challenge. It is a command line program that reads all files in a directory and encodes them into an mp4 video.

### Usage

This is an sbt project which uses sbt-assembly to package a jar containing all code and dependencies. To assemble the jar:
```
    $ sbt assembly
```

It can then be run with scala-2.12: (note that increasing memory maybe required to store the mp4 video)
```
    $ scala -J-Xmx2G target/scala-2.12/video_encoder-assembly-1.0.jar ./path/to/images/
```

Alternatively, it can be ran without packaging:
```
    $ sbt "run ./path/to/images"
```

### Compatibility issues

I ran into an issue where the output .mp4 file only showed 1 frame when played with VLC (on Arch linux). It does successful play using QuickTime on Mac.

### Improvements

* Make greater use of mapping over scalaz disjunctions, as opposed to matching on cases.
* Use scalaz Tasks to encode images concurrently. (this might effect the frame ordering in the internal Mp4Video object).