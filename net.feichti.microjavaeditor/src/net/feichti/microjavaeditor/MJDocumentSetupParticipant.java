package net.feichti.microjavaeditor;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;

public class MJDocumentSetupParticipant implements IDocumentSetupParticipant
{
	@Override
	public void setup(IDocument document) {
		MJPartitionScanner partitionScanner = MicroJavaEditorPlugin.getPartitionScanner();
		IDocumentPartitioner partitioner =
				new FastPartitioner(partitionScanner, MJPartitionScanner.MICROJAVA_PARTITION_TYPES);
		
		if(document instanceof IDocumentExtension3) {
			IDocumentExtension3 e3 = (IDocumentExtension3)document;
			e3.setDocumentPartitioner(MicroJavaEditorPlugin.MICROJAVA_PARTITIONING, partitioner);
		} else {
			document.setDocumentPartitioner(partitioner);
		}
		partitioner.connect(document);
	}
}
