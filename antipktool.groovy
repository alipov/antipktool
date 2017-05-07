import java.io.InputStream
import java.io.OutputStream
import java.nio.file.*

println ''
if (args.length != 1) {
    println "Usage: antipktool path-to-apk"
    exit()
}

modifyManifest(args[0])

// https://github.com/iBotPeaches/Apktool/issues/1435
def modifyManifest(apkPath) {
    Path apkFilePath = Paths.get(apkPath)
    FileSystem fs = null
    try {
        fs = FileSystems.newFileSystem(apkFilePath, null)
        Path manifestPath = fs.getPath("/AndroidManifest.xml")
        Path manifestTempPath = fs.getPath("/___AndroidManifestTemp.xml")
        Files.move(manifestPath, manifestTempPath)
        streamCopy(manifestTempPath, manifestPath)
        Files.delete(manifestTempPath)
        println 'modified!'
    } catch (IOException e) {
        e.printStackTrace()
        exit()
    } finally {
        fs.close()
    }
}

private static void streamCopy(Path src, Path dst) throws IOException {
    byte[] buf = new byte[8192]
    InputStream isSrc = null
    OutputStream osDst = null
    try {
        isSrc = Files.newInputStream(src)
        osDst = Files.newOutputStream(dst)
        int n = 0;

        if ((n = isSrc.read(buf, 0, 4)) != -1) {
            buf[0] = 0
            osDst.write(buf, 0, n)
        } else {
            println "Couldn't read first four bytes"
            exit()
        }
        
        while ((n = isSrc.read(buf)) != -1) {
            osDst.write(buf, 0, n)
        }
    } finally {
        osDst.close()
        isSrc.close()
    }
}