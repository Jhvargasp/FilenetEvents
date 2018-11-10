package events;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import com.filenet.api.collection.ContentElementList;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.core.ContentTransfer;
import com.filenet.api.core.Document;
import com.filenet.api.core.Factory;
import com.filenet.api.engine.EventActionHandler;
import com.filenet.api.events.ObjectChangeEvent;
import com.filenet.api.util.Id;

public class JavaEventHandler implements EventActionHandler {
	ResourceBundle bundle = ResourceBundle.getBundle("events.config");
	List<String> docsAllowed = new ArrayList();

	public JavaEventHandler() {
		docsAllowed.addAll(Arrays.asList(bundle.getString("formatAllowed").split(";")));
	}

	public void onEvent(ObjectChangeEvent event, Id subId) {
		try {
			System.out.println("Object created successfully");
			System.out.println("Wait....");
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Continue....");

			Document doc = Factory.Document.fetchInstance(event.getObjectStore(), event.get_SourceObjectId(), null);
			ContentElementList celist = doc.get_ContentElements();
			if (celist.size() > 0) {
				ContentTransfer ce = (ContentTransfer) celist.get(0);
				String fname = ce.get_RetrievalName();
				System.out.println("Object name" + fname);
				boolean toDelete = false;
				for (String mime : docsAllowed) {
					if (fname.indexOf(mime) > 0) {
						toDelete = true;
					}
				}
				if (!toDelete) {
					toDelete = validateContentFile(ce.get_RetrievalName(), ce.accessContentStream());
				}

				if (toDelete) {
					System.out.println("log file created.. to delete..");
					doc.delete();
					doc.save(RefreshMode.NO_REFRESH);
					System.out.println("Document deleted successfully");
					/*try {
						String content = bundle.getString("mailContent");
						content = content.replaceAll("\\{0\\}", doc.get_Id().toString());
						content = content.replaceAll("\\{1\\}", bundle.getString("formatAllowed"));
						new SendHTMLEmail().sendEmail(bundle.getString("mailTo"), bundle.getString("mailSubject"),
								content);
					} catch (Exception e) {
						e.printStackTrace();
					}*/
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private boolean validateContentFile(String retrievalName, InputStream accessContentStream) {
		boolean isValid=false;
		BufferedReader fileReader = new BufferedReader(new InputStreamReader(accessContentStream));
		try {
			// Read the CSV file header to skip it
			String line = fileReader.readLine();

			// Read the file line by line starting from the second line
			//pdf and tif using this docs https://stackoverflow.com/questions/2731917/how-to-detect-if-a-file-is-pdf-or-tiff
			//jpg file using  http://vip.sugovica.hu/Sardi/kepnezo/JPEG%20File%20Layout%20and%20Format.htm
			if(retrievalName.toLowerCase().endsWith(".pdf") ) {
				if(line.indexOf("%PDF-")==0 ) {
					System.out.println("Is a valid pdf!");
					isValid=true;
				}else{
					System.out.println("WARNING..... THIS IS NOT A VALID PDF FILE!!!, "+retrievalName);
				}	
			} else if(retrievalName.toLowerCase().endsWith(".tif") ) {
				if(line.indexOf("II")==0 || line.indexOf("MM")==0 ) {
					System.out.println("Is a valid tif!");
					isValid=true;
				}else{
					System.out.println("WARNING..... THIS IS NOT A VALID PDF TIF!!!, "+retrievalName);
				}
			} else if(retrievalName.toLowerCase().endsWith(".jpg") ) {
				if(line.indexOf("ÿØÿà")==0 ) {
					System.out.println("Is a valid jpg!");
					isValid=true;
				}else{
					System.out.println("WARNING..... THIS IS NOT A VALID PDF JPG!!!, "+retrievalName);
				}
			}
			
		} catch (Exception e) {
			System.out.println("Error in validateContentFile !!!");
			e.printStackTrace();
		} finally {
			try {
				fileReader.close();
			} catch (IOException e) {
				System.out.println("Error while closing fileReader !!!");
				e.printStackTrace();
			}
		}

		return !isValid;
	}
	
	public static void main(String[] args) throws Exception{
		JavaEventHandler ev= new JavaEventHandler();
		String folder = "C:\\tmp\\images\\";
		String []ls=new File(folder).list();
		for (int i = 0; i < ls.length; i++) {
			String string = ls[i];
			boolean v=ev.validateContentFile(string, new FileInputStream(folder+string));
			System.out.println(string +" -- valida?"+v);
		}
		
	}
	
}
