package l.files.fs.media;

import l.files.fs.Path;

import static l.files.fs.media.MediaTypes.MEDIA_TYPE_OCTET_STREAM;

public final class MagicDetectorTest extends BasePropertyDetectorTest {

    @Override
    BasePropertyDetector detector() {
        return MagicDetector.INSTANCE;
    }

    public void test_detects_content_only_not_file_name() throws Exception {
        Path file = createTextFile("a.txt", "");
        assertEquals(MEDIA_TYPE_OCTET_STREAM, detector().detect(getContext(), file));
    }

}
