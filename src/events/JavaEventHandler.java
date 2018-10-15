package events;

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
	ResourceBundle bundle=ResourceBundle.getBundle("events.config");
	List<String>docsAllowed=new ArrayList();
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
				boolean toDelete=false;
				for (String mime : docsAllowed) {
					if (fname.indexOf(mime) > 0) {
						toDelete=true;
					}
				}
				if (toDelete) {
					System.out.println("log file created.. to delete..");
					doc.delete();
					doc.save(RefreshMode.NO_REFRESH);
					System.out.println("Document deleted successfully");
					try {
						String content=bundle.getString("mailContent");
						content=content.replaceAll("\\{0\\}", doc.get_Id().toString());
						content=content.replaceAll("\\{1\\}", bundle.getString("formatAllowed"));
						new SendHTMLEmail().sendEmail(bundle.getString("mailTo"), 
								bundle.getString("mailSubject"), content);
					}catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}
