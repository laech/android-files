package l.files.ui.browser;

import android.content.ContextWrapper;
import android.content.Intent;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.CountDownLatch;

import l.files.fs.BuildConfig;
import l.files.fs.FileSystem;
import l.files.fs.LinkOption;
import l.files.fs.Name;
import l.files.fs.Path;
import l.files.fs.Stat;

import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.robolectric.RuntimeEnvironment.application;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = JELLY_BEAN)
public final class OpenFileTest {

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void sends_correct_intent_for_apk_file() throws Exception {

        final String testFile = "open_file_test.apk";
        final FileSystem fs = mock(FileSystem.class);
        final Stat stat = mock(Stat.class);
        final Path file = mock(Path.class);
        final Name name = mock(Name.class);

        given(name.toString()).willReturn(testFile);
        given(stat.isRegularFile()).willReturn(true);
        given(file.name()).willReturn(name);
        given(file.toUri()).willReturn(URI.create("file:///tmp/" + testFile));
        given(file.fileSystem()).willReturn(fs);
        given(fs.stat(eq(file), any(LinkOption.class))).willReturn(stat);
        given(fs.newInputStream(file)).willAnswer(new Answer<InputStream>() {
            @Override
            public InputStream answer(InvocationOnMock invocation) throws Throwable {
                return OpenFileTest.class.getResourceAsStream(testFile);
            }
        });

        final CountDownLatch latch = new CountDownLatch(1);
        final String[] type = {null};
        ContextWrapper context = new ContextWrapper(application) {

            @Override
            public void startActivity(Intent intent) {
                type[0] = intent.getType();
                latch.countDown();
            }

        };

        new OpenFile(context, file, stat).execute();

        assertTrue(latch.await(10, SECONDS));
        assertEquals("application/vnd.android.package-archive", type[0]);

    }

}
