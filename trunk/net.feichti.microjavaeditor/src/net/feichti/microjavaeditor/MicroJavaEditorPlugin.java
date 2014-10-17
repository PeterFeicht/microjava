package net.feichti.microjavaeditor;

import java.net.URL;

import net.feichti.microjavaeditor.microjava.MJCodeScanner;
import net.feichti.microjavaeditor.microjava.MJCommentScanner;
import net.feichti.microjavaeditor.util.MJColorManager;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class MicroJavaEditorPlugin extends AbstractUIPlugin
{
	public static final String PLUGIN_ID = "net.feichti.microjavaeditor";
	public final static String MICROJAVA_PARTITIONING = "__microjava_partitioning";
	
	public static final IPath IMG_PATH = new Path("icons/");
	public static final String IMG_PROGRAM = "package_obj.gif";
	public static final String IMG_CLASS = "class_obj.gif";
	public static final String IMG_FIELD = "field_public_obj.gif";
	public static final String IMG_VARIABLE = "field_protected_obj.gif";
	public static final String IMG_CONSTANT_OVERLAY = "constr_ovr.gif";
	public static final String IMG_METHOD = "methpub_obj.gif";
	public static final String IMG_MAIN_OVERLAY = "run_co.gif";
	public static final String IMG_LOCAL = "localvariable_obj.gif";
	
	private static MicroJavaEditorPlugin sInstance;
	
	private MJPartitionScanner mPartitionScanner;
	private MJColorManager mColorManager;
	private MJCodeScanner mCodeScanner;
	private MJCommentScanner mCommentScanner;
	
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		sInstance = this;
	}
	
	@Override
	public void stop(BundleContext context) throws Exception {
		if(sInstance != null) {
			sInstance.dispose();
			sInstance = null;
		}
		super.stop(context);
	}
	
	private void dispose() {
		if(mColorManager != null) {
			mColorManager.dispose();
			mColorManager = null;
		}
	}
	
	/**
	 * Returns the shared instance.
	 */
	public static MicroJavaEditorPlugin getDefault() {
		return sInstance;
	}
	
	@Override
	protected void initializeImageRegistry(ImageRegistry reg) {
		createImageDescriptor(reg, IMG_PROGRAM);
		createImageDescriptor(reg, IMG_CLASS);
		createImageDescriptor(reg, IMG_FIELD);
		createImageDescriptor(reg, IMG_VARIABLE);
		createImageDescriptor(reg, IMG_CONSTANT_OVERLAY);
		createImageDescriptor(reg, IMG_METHOD);
		createImageDescriptor(reg, IMG_MAIN_OVERLAY);
		createImageDescriptor(reg, IMG_LOCAL);
	}
	
	private void createImageDescriptor(ImageRegistry reg, String key) {
		IPath path = IMG_PATH.append(key);
		URL url = FileLocator.find(getBundle(), path, null);
		ImageDescriptor descriptor;
		if(url != null) {
			descriptor = ImageDescriptor.createFromURL(url);
		} else {
			descriptor = imageDescriptorFromPlugin(PLUGIN_ID, path.toString());
		}
		reg.put(key, descriptor);
	}
	
	/**
	 * Get an image descriptor for the specified key.
	 * 
	 * @param key Either one of the {@code IMG_} constants, or a plugin-relative path to an icon
	 * @return Image descriptor for the key or path
	 */
	public static ImageDescriptor getImageDescriptor(String key) {
		ImageDescriptor d = sInstance.getImageRegistry().getDescriptor(key);
		if(d != null) {
			return d;
		}
		return imageDescriptorFromPlugin(PLUGIN_ID, key);
	}
	
	/**
	 * Get an image for the specified key.
	 * 
	 * @param key Either one of the {@code IMG_} constants, or a plugin-relative path to an icon
	 * @return Image for the key or path
	 */
	public static Image getImage(String key) {
		Image i = sInstance.getImageRegistry().get(key);
		if(i != null) {
			return i;
		}
		return getImageDescriptor(key).createImage();
	}
	
	/**
	 * Get the scanner for creating MicroJava partitions.
	 */
	public static MJPartitionScanner getPartitionScanner() {
		if(sInstance.mPartitionScanner == null) {
			sInstance.mPartitionScanner = new MJPartitionScanner();
		}
		return sInstance.mPartitionScanner;
	}
	
	/**
	 * Get the MicroJava color manager.
	 */
	public static MJColorManager getColorManager() {
		if(sInstance.mColorManager == null) {
			sInstance.mColorManager = new MJColorManager();
		}
		return sInstance.mColorManager;
	}
	
	/**
	 * Get the MicroJava code scanner.
	 */
	public static MJCodeScanner getCodeScanner() {
		if(sInstance.mCodeScanner == null) {
			sInstance.mCodeScanner = new MJCodeScanner(getColorManager());
		}
		return sInstance.mCodeScanner;
	}
	
	/**
	 * Get the MicroJava comment scanner.
	 */
	public static MJCommentScanner getCommentScanner() {
		if(sInstance.mCommentScanner == null) {
			sInstance.mCommentScanner = new MJCommentScanner(getColorManager());
		}
		return sInstance.mCommentScanner;
	}
}
