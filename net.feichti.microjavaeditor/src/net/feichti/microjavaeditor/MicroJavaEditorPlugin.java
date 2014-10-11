package net.feichti.microjavaeditor;

import net.feichti.microjavaeditor.microjava.MJCodeScanner;
import net.feichti.microjavaeditor.util.MJColorManager;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class MicroJavaEditorPlugin extends AbstractUIPlugin
{
	public static final String PLUGIN_ID = "net.feichti.microjavaeditor";
	public final static String MICROJAVA_PARTITIONING = "__microjava_partitioning";
	
	private static MicroJavaEditorPlugin sInstance;
	
	private MJPartitionScanner mPartitionScanner;
	private MJColorManager mColorManager;
	private MJCodeScanner mCodeScanner;
	
	/**
	 * The constructor
	 */
	public MicroJavaEditorPlugin() {
	}
	
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		sInstance = this;
	}
	
	@Override
	public void stop(BundleContext context) throws Exception {
		sInstance = null;
		super.stop(context);
	}
	
	/**
	 * Returns the shared instance.
	 */
	public static MicroJavaEditorPlugin getDefault() {
		return sInstance;
	}
	
	/**
	 * Returns an image descriptor for the image file at the given plug-in relative path.
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	
	/**
	 * Get the scanner for creating MicroJava partitions.
	 */
	public MJPartitionScanner getPartitionScanner() {
		if(mPartitionScanner == null) {
			mPartitionScanner = new MJPartitionScanner();
		}
		return mPartitionScanner;
	}
	
	/**
	 * Get the MicroJava color manager.
	 */
	public MJColorManager getColorManager() {
		if(mColorManager == null) {
			mColorManager = new MJColorManager();
		}
		return mColorManager;
	}
	
	/**
	 * Get the MicroJava code scanner.
	 */
	public MJCodeScanner getCodeScanner() {
		if(mCodeScanner == null) {
			mCodeScanner = new MJCodeScanner(getColorManager());
		}
		return mCodeScanner;
	}
}
