package net.feichti.microjavaeditor.wizards;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

import net.feichti.microjavaeditor.MJEditorMessages;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

public class NewProgramWizardPage extends WizardNewFileCreationPage
{
	public static final String COMMENT_TEMPLATE = "/*\n * \n */\n";
	public static final String MAIN_TEMPLATE = "void main() {\n\t\t\n\t}";
	
	private Button mChkCreateComment;
	private Button mChkCreateMain;
	
	public NewProgramWizardPage(IStructuredSelection selection) {
		super("newMicroJavaProgramPage", selection);
		setTitle(MJEditorMessages.getString("NewProgramWizard.PageTitle"));
		setDescription(MJEditorMessages.getString("NewProgramWizard.PageDescription"));
		setFileExtension("mj");
	}
	
	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		Composite main = (Composite)getControl();
		
		// Add a separator before MicroJava specific controls
		Label sep = new Label(main, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData gd = new GridData();
		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = GridData.BEGINNING;
		gd.heightHint = convertHeightInCharsToPixels(1);
		sep.setLayoutData(gd);
		
		// Create MicroJava specific controls
		Group group = new Group(main, SWT.NONE);
		group.setLayout(new GridLayout());
		group.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		group.setText(MJEditorMessages.getString("NewProgramWizard.CreateElementsGroup"));
		
		mChkCreateComment = new Button(group, SWT.CHECK);
		mChkCreateComment.setText(MJEditorMessages.getString("NewProgramWizard.CreateComment"));
		mChkCreateComment.setSelection(true);
		
		mChkCreateMain = new Button(group, SWT.CHECK);
		mChkCreateMain.setText(MJEditorMessages.getString("NewProgramWizard.CreateMainMethod"));
		mChkCreateMain.setSelection(true);
		
		// setPageComplete(validatePage());
	}
	
	@Override
	protected String getNewFileLabel() {
		return MJEditorMessages.getString("NewProgramWizard.FileNameLabel");
	}
	
	@Override
	protected boolean validatePage() {
		if(!super.validatePage()) {
			return false;
		}
		
		String name = getFileName().trim();
		if(name.endsWith(".mj") && name.substring(0, name.length() - 3).isEmpty()) {
			setErrorMessage(MJEditorMessages.getString("NewProgramWizard.NoEmptyName"));
			return false;
		}
		if(name.length() > 0 && !Character.isUpperCase(name.charAt(0))) {
			setMessage(MJEditorMessages.getString("NewProgramWizard.StartNameWithUpper"), WARNING);
		} else {
			setMessage(null);
		}
		
		setErrorMessage(null);
		return true;
	}
	
	@Override
	protected InputStream getInitialContents() {
		StringBuilder sb = new StringBuilder();
		String name = getFileName().trim();
		
		if(mChkCreateComment.getSelection()) {
			sb.append(COMMENT_TEMPLATE);
		}
		
		sb.append("program ");
		if(name.endsWith(".mj")) {
			sb.append(name.substring(0, name.length() - 3));
		} else {
			sb.append(name);
		}
		sb.append("\n{\n\t");
		
		if(mChkCreateMain.getSelection()) {
			sb.append(MAIN_TEMPLATE);
		}
		
		sb.append("\n}\n");
		
		return new ByteArrayInputStream(sb.toString().getBytes(Charset.forName("US-ASCII")));
	}
}
