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
import net.feichti.microjavaeditor.util.MJFileModel.VariableKind;

import org.antlr.v4.runtime.tree.ParseTree;
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
	public static final String MAIN_NAME = "main";
	private static final String UNKNOWN = "<unknown>";
	private static final String UNNAMED = "<unnamed>";
	
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
		return null;
	}
	
	@Override
	public StyledString getStyledText(Object element) {
		String name, type;
		
		if(element instanceof ClassDeclContext) {
			ClassDeclContext clazz = (ClassDeclContext)element;
			name = getText(clazz.Ident(), UNNAMED);
			type = "";
			
		} else if(element instanceof VarDeclWrapper) {
			VarDeclWrapper var = (VarDeclWrapper)element;
			name = getText(var.getIdent(), UNNAMED);
			type = getText(var.getType(), UNKNOWN);
			
		} else if(element instanceof ConstDeclContext) {
			ConstDeclContext con = (ConstDeclContext)element;
			name = getText(con.Ident(), UNNAMED);
			type = getText(con.type(), UNKNOWN);
			
		} else if(element instanceof MethodDeclContext) {
			MethodDeclContext method = (MethodDeclContext)element;
			StringBuilder sb = new StringBuilder()
					.append(getText(method.Ident(), UNNAMED))
					.append('(');
			
			FormParsContext pars = method.formPars();
			if(pars != null) {
				Iterator<TypeContext> types = pars.type().iterator();
				if(types.hasNext()) {
					sb.append(getText(types.next(), "??"));
				}
				while(types.hasNext()) {
					sb.append(", ");
					sb.append(getText(types.next(), "??"));
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
				type = UNKNOWN;
			}
			
		} else if(element instanceof ProgContext) {
			ProgContext prog = (ProgContext)element;
			name = getText(prog.Ident(), UNNAMED);
			type = "program";
			
		} else if(element instanceof String) {
			name = (String)element;
			type = "";
			
		} else {
			name = UNKNOWN;
			type = element.getClass().getName();
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
			VariableKind kind = VariableKind.forDeclaration(((VarDeclWrapper)element).getContext());
			if(kind != null) {
				key = kind.imageKey;
			}
			
		} else if(element instanceof ConstDeclContext) {
			return mConstImage;
			
		} else if(element instanceof MethodDeclContext) {
			MethodDeclContext method = (MethodDeclContext)element;
			if(MAIN_NAME.equals(getText(method.Ident(), null))) {
				return mMainImage;
			}
			key = MicroJavaEditorPlugin.IMG_METHOD;
			
		} else if(element instanceof ProgContext) {
			key = MicroJavaEditorPlugin.IMG_PROGRAM;
			
		}
		
		return (key != null ? MicroJavaEditorPlugin.getDefault().getImage(key) : null);
	}
	
	/**
	 * Get the text of the specified parse tree, or a default value if {@code n} is {@code null}.
	 * 
	 * @param n A {@link ParseTree}, can be {@code null}
	 * @param def The default value
	 * @return {@link ParseTree#getText() n.getText()} if {@code n} is not {@code null}, {@code def} otherwise
	 */
	private static String getText(ParseTree n, String def) {
		return (n != null ? n.getText() : def);
	}
}
