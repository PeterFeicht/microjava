package net.feichti.microjavaeditor.wizards;

import net.feichti.microjavaeditor.MJEditorMessages;
import net.feichti.microjavaeditor.MicroJavaEditorPlugin;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

public class NewProgramWizard extends Wizard implements INewWizard
{
	public static final String WIZARD_ID = "net.feichti.microjavaeditor.wizards.new.program";
	
	private IWorkbench mWorkbench;
	private IStructuredSelection mSelection;
	private NewProgramWizardPage mPage;
	
	@Override
	public void addPages() {
		mPage = new NewProgramWizardPage(mSelection);
		addPage(mPage);
	}
	
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		mWorkbench = workbench;
		mSelection = selection;
		setWindowTitle(MJEditorMessages.getString("NewProgramWizard.WindowTitle"));
		setNeedsProgressMonitor(true);
		
		 ImageDescriptor pageImage = MicroJavaEditorPlugin.getImageDescriptor("icons/newfile_wizban.png");
		 setDefaultPageImageDescriptor(pageImage);
	}
	
	@Override
	public boolean performFinish() {
		IFile file = mPage.createNewFile();
		if(file == null) {
			return false;
		}
		
		BasicNewResourceWizard.selectAndReveal(file, mWorkbench.getActiveWorkbenchWindow());
		
		// Open editor on new file
		IWorkbenchWindow dw = getWorkbench().getActiveWorkbenchWindow();
		if(dw != null) {
			try {
				IWorkbenchPage page = dw.getActivePage();
				if(page != null) {
					IDE.openEditor(page, file, true);
				}
			} catch(PartInitException ex) {
				// Shamelessly copied from org.eclipse.ui.internal.ide.DialogUtil
				CoreException nestedException = null;
				IStatus status = ex.getStatus();
				if(status != null && status.getException() instanceof CoreException) {
					nestedException = (CoreException)status.getException();
				}
				if(nestedException != null) {
					// Open an error dialog and include the extra status information from the nested
					// CoreException
					ErrorDialog.openError(dw.getShell(), MJEditorMessages.getString("NewProgramWizard.EditorError"),
							ex.getLocalizedMessage(), nestedException.getStatus());
				} else {
					// Open a regular error dialog since there is no extra information to display.
					MessageDialog.openError(dw.getShell(), MJEditorMessages.getString("NewProgramWizard.EditorError"),
							ex.getLocalizedMessage());
				}
			}
		}
		
		return true;
	}
	
	public IWorkbench getWorkbench() {
		return mWorkbench;
	}
	
	public IStructuredSelection getSelection() {
		return mSelection;
	}
}
