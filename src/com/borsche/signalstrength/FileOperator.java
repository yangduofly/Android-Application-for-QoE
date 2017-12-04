package com.borsche.signalstrength;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class FileOperator {
	
	private File mCurrentFile;
	private FileOutputStream mOutputStream;
	private OutputStreamWriter mOutputStreamWriter;
	
	public void CreateNewFile(String fileName) throws FileNotFoundException{
		mCurrentFile = new File(fileName);
		mOutputStream = new FileOutputStream(mCurrentFile);
		mOutputStreamWriter = new OutputStreamWriter(mOutputStream);
	}
	
	public void WriteText(String data) throws IOException {
		mOutputStreamWriter.write(data);
		mOutputStreamWriter.flush();
	}
	
	public void CloseFile() throws IOException{
		if(mOutputStreamWriter != null)
			mOutputStreamWriter.close();
	}
}
