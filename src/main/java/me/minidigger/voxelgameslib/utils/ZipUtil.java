package me.minidigger.voxelgameslib.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import javax.annotation.Nonnull;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;

/**
 * Small util to make working with zips easier
 */
public class ZipUtil {

  /**
   * Creates a zip from all files and folders in the specified folder. DOES NOT INCLUDE THE FOLDER
   * ITSELF!
   *
   * @param file the folder which content should be zipped
   * @return the created zip
   * @throws ZipException if something goes wrong
   */
  @Nonnull
  public static ZipFile createZip(@Nonnull File file) throws ZipException {
    ZipFile zip = new ZipFile(new File(file.getParent(), file.getName() + ".zip"));
    ArrayList<File> fileList = new ArrayList<>();

    File[] files = file.listFiles();
    if (files == null) {
      return zip;
    }

    fileList.addAll(Arrays.asList(files));

    zip.createZipFile(fileList, new ZipParameters());

    return zip;
  }
}