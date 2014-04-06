package gov.nist.appvet.shared;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Zip {
	List<String> fileList;
	private static String OUTPUT_ZIP_FILE = null; 
	private static String SOURCE_FOLDER = null; 

	public Zip() {
		fileList = new ArrayList<String>();
	}

	public boolean zipDir(String sourceFolder, String zipFilePath) {
		SOURCE_FOLDER = sourceFolder;
		OUTPUT_ZIP_FILE = zipFilePath;
		generateFileList(new File(sourceFolder));
		return zipIt(zipFilePath);
	}

	public static void main(String[] args) {
		Zip appZip = new Zip();
		appZip.generateFileList(new File(SOURCE_FOLDER));
		appZip.zipIt(OUTPUT_ZIP_FILE);
	}

	public boolean zipIt(String zipFile) {

		byte[] buffer = new byte[1024];

		try {
			FileOutputStream fos = new FileOutputStream(zipFile);
			ZipOutputStream zos = new ZipOutputStream(fos);

			for (String file : this.fileList) {

				ZipEntry ze = new ZipEntry(file);
				zos.putNextEntry(ze);

				FileInputStream in = new FileInputStream(SOURCE_FOLDER
						+ File.separator + file);

				int len;
				while ((len = in.read(buffer)) > 0) {
					zos.write(buffer, 0, len);
				}

				in.close();
			}

			zos.closeEntry();
			zos.close();
			return true;
		} catch (IOException ex) {
			ex.printStackTrace();
			return false;
		}
	}

	public void generateFileList(File node) {

		// add file only
		if (node.isFile()) {
			fileList.add(generateZipEntry(node.getAbsoluteFile().toString()));
		}

		if (node.isDirectory()) {
			String[] subNote = node.list();
			for (String filename : subNote) {
				generateFileList(new File(node, filename));
			}
		}

	}

	private String generateZipEntry(String file) {
		return file.substring(SOURCE_FOLDER.length() + 1, file.length());
	}
}
