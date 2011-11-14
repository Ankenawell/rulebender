package rulebender.influencegraph.view;

import java.awt.Dimension;
import java.io.IOException;
import java.util.HashMap;

import org.antlr.runtime.RecognitionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

import bngparser.grammars.BNGGrammar.prog_return;

import rulebender.contactmap.models.CMapModel;
import rulebender.contactmap.models.CMapModelBuilder;
import rulebender.contactmap.prefuse.CMapVisual;
import rulebender.contactmap.view.ContactMapView;
import rulebender.core.utility.BNGParserCommands;
import rulebender.editors.bngl.model.BNGASTReader;
import rulebender.influencegraph.models.IGraphModel;
import rulebender.influencegraph.models.InfluenceGraphModelBuilder;
import rulebender.influencegraph.prefuse.IGraphVisual;

public class InfluenceGraphSelectionListener implements ISelectionListener 
{
	private InfluenceGraphView m_view;
	
	private String currentFile;
	
	private HashMap<String, prefuse.Display> influenceGraphRegistry;
	
	public InfluenceGraphSelectionListener(InfluenceGraphView influenceGraphView)
	{
		setView(influenceGraphView);
		
		// Create the registry
		influenceGraphRegistry = new HashMap<String, prefuse.Display>();
		
		// Register the view as a listener for workbench selections.
		m_view.getSite().getPage().addPostSelectionListener(this);
	}

	private void setView(InfluenceGraphView view) 
	{
		m_view = view;	
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) 
	{
		if(part.getClass().toString().contains("rulebender.editors"))
		{
			editorSelection(part, selection);
		}
		
	}
	
	private void editorSelection(IWorkbenchPart part, ISelection selection)
	{
		// Check to see if it is the editor.
		// TODO  Right now I am just seeing if the name of the selection is a path to a bngl file. 
		// There is probably a better way to do this.
		if(part.getTitle().contains(".") && part.getTitle().substring(part.getTitle().lastIndexOf(".")).equals(".bngl"))
		{
			// If it's the same file
			if(part.getTitle().equals(currentFile))
			{
				//TODO it could be a text selection.
				System.out.println("same file");
			}
			// If it's a different file, then call the local private method that 
			// handles it. 
			else
			{
				newFileSelected(part.getTitle());
			}
		}
		// If it's not a bngl file
		else
		{
			currentFile = "";
			m_view.setIGraph(null);
		}
	}
	
	/**
	 * Private method to handle when a new file is selected. 
	 */
	private void newFileSelected(String fileName)
	{
		// Set the current file.
		currentFile = fileName;
		
		// Clear the contact map
		m_view.setIGraph(null);
		
		// Try to get an existing map.
		prefuse.Display toShow = influenceGraphRegistry.get(currentFile);
		
		// If it does not exist yet then generate it and add it to the registry.
		if(toShow == null)
		{
			toShow = generateInfluenceGraph(currentFile);
			influenceGraphRegistry.put(currentFile, toShow);
		}
		
		// Set the correct contact map.
		m_view.setIGraph(toShow);
	}
	
	/**
	 * Generates a contact map using the antlr parser for a filename. 
	 * @param fileName
	 * @return
	 */
	private prefuse.Display generateInfluenceGraph(String fileName)
	{
		prog_return ast = null;
		
		// Get the ast
		try 
		{
			ast = BNGParserCommands.getASTForFileName(fileName);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RecognitionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Null check for the ast
		if(ast == null)
		{
			System.out.println("The AST is null.\nExiting...");
		}
		else
		{
			// print it out if it is good.
			System.out.println(ast.toString()+"\n\n================================================================");
		}
		
		// Set a dimension TODO get the correct dimension
		Dimension dim = m_view.getSize();

		// Create the builder for the cmap
		InfluenceGraphModelBuilder iGraphModelBuilder = new InfluenceGraphModelBuilder();
		// Create the astReader and register the cmapModelBuilder
		BNGASTReader astReader = new BNGASTReader(iGraphModelBuilder);
		// Use the reader to construct the model for the given ast.
		astReader.buildWithAST(ast);
		// Get the model from the builder.		
		IGraphModel iModel = iGraphModelBuilder.getIGraphModel();
		
		if(iModel == null)
		{
			System.out.println("The CMapModel is null.\nExiting");
			System.exit(0);
		}

		// Get the CMapVisual object for the CMapModel
		IGraphVisual iVisual = new IGraphVisual(iModel, dim);
		
		return iVisual.getDisplay();
	}
	
	/**
	 *TODO
	 * Temporary way to refresh the contact map.
	 * This will be deleted (or changed) when the editor can
	 * send the refresh actions. 
	 */
	public void tempRefresh()
	{
		m_view.setIGraph(null);
		m_view.setIGraph(generateInfluenceGraph(currentFile));
	}

}
