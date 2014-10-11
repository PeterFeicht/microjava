package net.feichti.microjavaeditor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.ui.editors.text.FileDocumentProvider;

public class MJDocumentProvider extends FileDocumentProvider
{
	@Override
	protected IDocument createDocument(Object element) throws CoreException {
		IDocument document = super.createDocument(element);
		if(document != null) {
			IDocumentPartitioner partitioner =
					new FastPartitioner(new MJPartitionScanner(), new String[] {
							MJPartitionScanner.MICROJAVA_COMMENT
					});
			partitioner.connect(document);
			document.setDocumentPartitioner(partitioner);
		}
		return document;
	}
}
