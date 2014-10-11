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
		if(document instanceof IDocumentExtension3) {
			IDocumentExtension3 e3 = (IDocumentExtension3)document;
			IDocumentPartitioner partitioner =
					new FastPartitioner(MicroJavaEditorPlugin.getDefault().getPartitionScanner(),
							MJPartitionScanner.MICROJAVA_PARTITION_TYPES);
			e3.setDocumentPartitioner(MicroJavaEditorPlugin.MICROJAVA_PARTITIONING, partitioner);
			partitioner.connect(document);
		}
	}
}
