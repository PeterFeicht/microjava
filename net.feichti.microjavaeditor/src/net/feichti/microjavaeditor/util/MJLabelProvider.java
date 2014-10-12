package net.feichti.microjavaeditor.util;

import java.util.Iterator;

import net.feichti.microjavaeditor.MicroJavaEditorPlugin;
import net.feichti.microjavaeditor.antlr4.MicroJavaParser;
import net.feichti.microjavaeditor.antlr4.MicroJavaParser.ClassDeclContext;
import net.feichti.microjavaeditor.antlr4.MicroJavaParser.ConstDeclContext;
import net.feichti.microjavaeditor.antlr4.MicroJavaParser.FormParsContext;
import net.feichti.microjavaeditor.antlr4.MicroJavaParser.MethodDeclContext;
import net.feichti.microjavaeditor.antlr4.MicroJavaParser.ProgContext;
import net.feichti.microjavaeditor.antlr4.MicroJavaParser.TypeContext;
import net.feichti.microjavaeditor.antlr4.MicroJavaParser.VarDeclContext;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.IToolTipProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;

/**
 * Provides labels for various MicroJava elements.
 * 
 * @author Peter
 */
public class MJLabelProvider extends BaseLabelProvider implements IStyledLabelProvider, IToolTipProvider
{
	private Image mConstImage;
	private Image mMainImage;
	
	/**
	 * This is a convenience method that creates a {@link DelegatingStyledCellLabelProvider} with an
	 * {@link MJLabelProvider} as its {@link IStyledLabelProvider}.
	 * 
	 * @return A DelegatingStyledCellLabelProvider instance
	 */
	public static DelegatingStyledCellLabelProvider create() {
		return new DelegatingStyledCellLabelProvider(new MJLabelProvider());
	}
	
	public MJLabelProvider() {
		MicroJavaEditorPlugin plugin = MicroJavaEditorPlugin.getDefault();
		ImageDescriptor d = new DecorationOverlayIcon(plugin.getImage(MicroJavaEditorPlugin.IMG_VARIABLE),
				plugin.getImageDescriptor(MicroJavaEditorPlugin.IMG_CONSTANT_OVERLAY), IDecoration.TOP_RIGHT);
		mConstImage = d.createImage();
		d = new DecorationOverlayIcon(plugin.getImage(MicroJavaEditorPlugin.IMG_METHOD),
				plugin.getImageDescriptor(MicroJavaEditorPlugin.IMG_MAIN_OVERLAY), IDecoration.TOP_LEFT);
		mMainImage = d.createImage();
	}
	
	@Override
	public void dispose() {
		super.dispose();
		mConstImage.dispose();
		mMainImage.dispose();
	}
	
	@Override
	public String getToolTipText(Object element) {
		if(element instanceof ConstDeclContext) {
			ConstDeclContext con = (ConstDeclContext)element;
			return con.literal().getText();
		}
		return null;
	}
	
	@Override
	public StyledString getStyledText(Object element) {
		String name, type;
		
		if(element instanceof ClassDeclContext) {
			ClassDeclContext clazz = (ClassDeclContext)element;
			name = clazz.Ident().getText();
			type = "";
			
		} else if(element instanceof VarDeclWrapper) {
			VarDeclWrapper var = (VarDeclWrapper)element;
			name = var.getIdent().getText();
			type = var.getType().getText();
			
		} else if(element instanceof ConstDeclContext) {
			ConstDeclContext con = (ConstDeclContext)element;
			name = con.Ident().getText();
			type = con.type().getText();
			
		} else if(element instanceof MethodDeclContext) {
			MethodDeclContext method = (MethodDeclContext)element;
			StringBuilder sb = new StringBuilder();
			sb.append(method.Ident().getText()).append('(');
			
			FormParsContext pars = method.formPars();
			if(pars != null) {
				Iterator<TypeContext> types = pars.type().iterator();
				if(types.hasNext()) {
					sb.append(types.next().getText());
				}
				while(types.hasNext()) {
					sb.append(", ").append(types.next().getText());
				}
			}
			sb.append(')');
			name = sb.toString();
			
			TypeContext t = method.type();
			if(t != null) {
				type = t.getText();
			} else if(method.getToken(MicroJavaParser.VOID, 0) != null) {
				type = "void";
			} else {
				type = "??";
			}
			
		} else if(element instanceof ProgContext) {
			ProgContext prog = (ProgContext)element;
			name = prog.Ident().getText();
			type = "program";
			
		} else {
			name = "<unknown>";
			type = element.getClass().getSimpleName();
		}
		
		StyledString ret = new StyledString(name);
		if(!type.isEmpty()) {
			ret.append(" : ", StyledString.DECORATIONS_STYLER);
			ret.append(type, StyledString.DECORATIONS_STYLER);
		}
		return ret;
	}
	
	@Override
	public Image getImage(Object element) {
		String key = null;
		if(element instanceof ClassDeclContext) {
			key = MicroJavaEditorPlugin.IMG_CLASS;
			
		} else if(element instanceof VarDeclWrapper) {
			VarDeclContext varDecl = ((VarDeclWrapper)element).getContext();
			if(varDecl.parent instanceof MethodDeclContext) {
				key = MicroJavaEditorPlugin.IMG_LOCAL;
			} else if(varDecl.parent instanceof ClassDeclContext) {
				key = MicroJavaEditorPlugin.IMG_FIELD;
			} else {
				key = MicroJavaEditorPlugin.IMG_VARIABLE;
			}
			
		} else if(element instanceof ConstDeclContext) {
			return mConstImage;
			
		} else if(element instanceof MethodDeclContext) {
			MethodDeclContext method = (MethodDeclContext)element;
			if("main".equals(method.Ident().getSymbol().getText())) {
				return mMainImage;
			}
			key = MicroJavaEditorPlugin.IMG_METHOD;
			
		} else if(element instanceof ProgContext) {
			key = MicroJavaEditorPlugin.IMG_PROGRAM;
			
		}
		
		return (key != null ? MicroJavaEditorPlugin.getDefault().getImage(key) : null);
	}
}
