package bean;

import java.io.File;
import java.sql.Date;
import java.util.LinkedList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.PostActivate;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.component.UISelectItems;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletResponse;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Identity;
import org.richfaces.model.UploadItem;

import util.JSFNavigationConstants;
import util.SpicsPermissions;
import util.excel.FullTrialDataExport;
import util.plugin.IPageFragment;
import util.plugin.IPluginRegistry;
import util.xml.IXMLImportExport;
import util.xml.XMLImportExport;
import util.xml.XmlImportExportException;
import db.interfaces.ITrialDataDAO;
import db.interfaces.ITrialFormDAO;
import entities.Attribute;
import entities.AttributeGroup;
import entities.Trial;
import entities.TrialData;
import entities.TrialForm;
import entities.User;

@Stateful
@Scope(ScopeType.SESSION)
@Name("ViewImportExport")
public class ViewImportExportBean implements ViewImportExport {

	private static final long serialVersionUID = 1L;

	@Logger
	private Log log;

	@EJB
	private ITrialFormDAO trialFormDAO;
	
	@EJB
	private ITrialDataDAO trialDataDAO;

	@In
	private SessionInfo sessionInfo;

	@In
	User user;
	
	@In("PluginRegistry")
	private IPluginRegistry pluginRegistry;

	private Long trialFormExportTfId;


	private transient List<UploadItem> uploads = new LinkedList<UploadItem>();

	/* ui specific */
	public UISelectItems getTrialFormSelectItems() {
		UISelectItems items = new UISelectItems();
		items.setId(getUid());
		LinkedList<SelectItem> selectList = new LinkedList<SelectItem>();
		List<TrialForm> trialForms = sessionInfo.getTrial().getTrialForms();
		
		if(trialForms.size() == 0) {
			SelectItem item = new SelectItem("");
			selectList.add(item);
		} else {
		
			trialFormExportTfId = trialForms.get(0).getId();
			
			for (TrialForm tf : trialForms) {
				SelectItem item = new SelectItem();
				item.setLabel(tf.getName() == null ? "" : tf.getName());
				item.setValue(tf.getId());
				selectList.add(item);
			}
		}
		items.setValue(selectList);
		return items;
	}
	
	public void setTrialFormSelectItems(UISelectItems selectItems) {	}	// required by JSF 1.2

	/* logic */
	public String importTrialForm() {
		try {
			
			if(uploads.size() > 0) {
			
				UploadItem ui = uploads.get(0);
				
				IXMLImportExport xmlExport = new XMLImportExport();
				
				TrialForm uploaded = xmlExport.readTrialFormFromZip(ui.getFile());
				
				log.info("sucessfully unmarshalled trialform #0", uploaded.getName());
				
				Trial t = sessionInfo.getTrial();
				
				uploaded.setTrial(t);
				uploaded.setLastModified(new Date(System.currentTimeMillis()));
				uploaded.setSort(t.getTrialForms().size());
				
				// rewire bidirectional assocations
				for(AttributeGroup ag : uploaded.getAttributeGroups()) {
					ag.setTrialForm(uploaded);
					for(Attribute a : ag.getAttributes()) {
						a.setAttributeGroup(ag);
					}
				}
				
				t.getTrialForms().add(uploaded);
				
				trialFormDAO.persist(uploaded);
				
				log.info("Trialform #0 sucessfully imported!", uploaded.getName());
				FacesMessages.instance().addFromResourceBundle(Severity.INFO, "trialformuploaded.info");
				
			} else {
				log.warn("importTrialForm: no file uploaded");
				FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "error.uploadbeforeimport");
			}
		} catch (XmlImportExportException e) {
			log.error("Import of trialform failed, reason: #0", e.getMessage());
			log.error("Printing stack trace");
			e.printStackTrace();
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "error.importfailed");
		} 
		
		uploads = new LinkedList<UploadItem>();
		return JSFNavigationConstants.RELOADPAGE;
	}
	
	/* getters and setters */
	public List<UploadItem> getUploads() {
		return uploads;
	}

	public void setUploads(List<UploadItem> uploads) {
		this.uploads = uploads;
	}	

	public Long getTrialFormExportTfId() {
		return trialFormExportTfId;
	}

	public void setTrialFormExportTfId(Long trialFormExportTfId) {
		this.trialFormExportTfId = trialFormExportTfId;
	}
	
	public boolean canImportTrialForm() {
		return sessionInfo.getTrial().isEditable() && 
			Identity.instance().hasPermission(sessionInfo.getTrial(), SpicsPermissions.EDIT_TRIAL_FORMS);
	}
	
	@PostActivate
	public void postActivate() {
		this.uploads = new LinkedList<UploadItem>();
	}
	
	public void exportAllTrialData() {
		performExport(null);
	}
	
	public void exportOwnTrialData() {
		performExport(user);
	}
	
	private void performExport(User user) {
		Trial trial = sessionInfo.getTrial();

		FullTrialDataExport ftde = new FullTrialDataExport(trial, user == null);
		List<TrialData> toExport;
		
		// generate filename of file to export the data into
		String filename = "";
		if(user == null) {
			Identity.instance().checkPermission(trial, SpicsPermissions.TRIAL_DATA_FULL_EXPORT);
			toExport = trialDataDAO.getTrialDatasForFullExport(trial.getId());
			filename = "fullexport.csv";
		} else {
			toExport = trialDataDAO.getTrialDatasForPersonalExport(trial.getId(), user.getUsername());
			filename = user.getUsername() + "_export.csv";
		}
		
		// TODO how to make path configurable?
		File exportedFile = new File(System.getProperty("jboss.server.temp.dir"), filename);
		
		for (int i = 0; i < 20; i++) {
			toExport.addAll(toExport);
		}
		
		// write data to export file
		ftde.exportAsCSV(exportedFile, toExport);
		
		HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
		response.setHeader("Content-disposition","attachement; filename=\"" + exportedFile.getName() + "\"");
		response.setContentLength(new Long(exportedFile.length()).intValue());
		response.setContentType("text/csv");
		response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
		response.setHeader("Location", "http://www.google.com/");
		/*
		// write binary data to servlet response output stream
        InputStream in = null;   
        ServletOutputStream sosStream = null;   
        try {  
            in = new FileInputStream(exportedFile);  
            sosStream = response.getOutputStream();   
            int ibit = 256;   
            while ((ibit) >= 0){   
                ibit = in.read();   
                sosStream.write(ibit);   
            }  
            sosStream.flush();  
            sosStream.close();  
      
            FacesContext.getCurrentInstance().responseComplete();
        } catch (IOException exp){   
        	exp.printStackTrace();
        } finally {
        	if (in != null) {
        		try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
        	}
           	if (sosStream != null) {
           		try {
					sosStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
        	}
           	exportedFile.delete();
        }*/
		FacesContext.getCurrentInstance().responseComplete();
	}

	@Remove
	@Destroy
	public void destroy() {
		System.out.println("ViewImportExportBean destroyed...");
	}
	
	private String getUid() {
		return FacesContext.getCurrentInstance().getViewRoot().createUniqueId();
	}
	
	public List<IPageFragment> getImportExportFragments() {
		return pluginRegistry.getPageFragments(PAGE_ID);
	}

}
