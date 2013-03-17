package com.example.files.util.media

import java.io.File
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.project.MavenProject

/**
 * @goal generate
 * @phase generate-sources
 */
class MediaMojo extends AbstractMojo {

  /**
   * @parameter default-value="${project.basedir}/AndroidManifest.xml"
   * @required
   * @readonly
   */
  private File androidManifest;

  /**
   * @parameter expression="${project.build.sourceDirectory}"
   * @required
   * @readonly
   */
  private File outputDirectory;

  /**
   * @parameter expression="${project}"
   * @required
   * @readonly
   */
  private MavenProject project;

  override execute() {
    MediaGenerator::generate(androidManifest, outputDirectory)
    project.addCompileSourceRoot(outputDirectory.absolutePath)
  }

}
