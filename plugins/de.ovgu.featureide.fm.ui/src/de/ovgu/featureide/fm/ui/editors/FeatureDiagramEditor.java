/* FeatureIDE - An IDE to support feature-oriented software development
 * Copyright (C) 2005-2011  FeatureIDE Team, University of Magdeburg
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 *
 * See http://www.fosd.de/featureide/ for further information.
 */
package de.ovgu.featureide.fm.ui.editors;

import org.eclipse.core.commands.operations.ObjectUndoContext;
import org.eclipse.draw2d.ConnectionLayer;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.EditDomain;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.KeyHandler;
import org.eclipse.gef.KeyStroke;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.actions.ZoomInAction;
import org.eclipse.gef.ui.actions.ZoomOutAction;
import org.eclipse.gef.ui.parts.GraphicalViewerImpl;
import org.eclipse.gef.ui.parts.GraphicalViewerKeyHandler;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchActionConstants;

import de.ovgu.featureide.fm.core.FeatureModel;
import de.ovgu.featureide.fm.ui.editors.featuremodel.GUIDefaults;
import de.ovgu.featureide.fm.ui.editors.featuremodel.actions.AbstractAction;
import de.ovgu.featureide.fm.ui.editors.featuremodel.actions.AlternativeAction;
import de.ovgu.featureide.fm.ui.editors.featuremodel.actions.AndAction;
import de.ovgu.featureide.fm.ui.editors.featuremodel.actions.CreateCompoundAction;
import de.ovgu.featureide.fm.ui.editors.featuremodel.actions.CreateConstraintAction;
import de.ovgu.featureide.fm.ui.editors.featuremodel.actions.CreateLayerAction;
import de.ovgu.featureide.fm.ui.editors.featuremodel.actions.DeleteAction;
import de.ovgu.featureide.fm.ui.editors.featuremodel.actions.EditConstraintAction;
import de.ovgu.featureide.fm.ui.editors.featuremodel.actions.HiddenAction;
import de.ovgu.featureide.fm.ui.editors.featuremodel.actions.LegendAction;
import de.ovgu.featureide.fm.ui.editors.featuremodel.actions.LegendLayoutAction;
import de.ovgu.featureide.fm.ui.editors.featuremodel.actions.MandatoryAction;
import de.ovgu.featureide.fm.ui.editors.featuremodel.actions.OrAction;
import de.ovgu.featureide.fm.ui.editors.featuremodel.actions.RenameAction;
import de.ovgu.featureide.fm.ui.editors.featuremodel.actions.ReverseOrderAction;
import de.ovgu.featureide.fm.ui.editors.featuremodel.editparts.GraphicalEditPartFactory;
import de.ovgu.featureide.fm.ui.editors.featuremodel.layouts.FeatureDiagramLayoutManager;
import de.ovgu.featureide.fm.ui.editors.featuremodel.layouts.LevelOrderLayout;

/**
 * An editor based on the Graphical Editing Framework to view and edit feature
 * diagrams and cross-tree constraints.
 * 
 * @author Thomas Thuem
 */
public class FeatureDiagramEditor implements GUIDefaults {
	
	private FeatureModelEditor featureModelEditor;

	//TODO extends ???
	private GraphicalViewerImpl graphicalViewer;

	private ZoomManager zoomManager;

	private ScalableFreeformRootEditPart rootEditPart;

	private FeatureDiagramLayoutManager layoutManager = new LevelOrderLayout();

//TODO	private boolean dirty = false;

	private CreateLayerAction createLayerAction;

	private CreateCompoundAction createCompoundAction;

	private DeleteAction deleteAction;

	private MandatoryAction mandatoryAction;

	private AbstractAction abstractAction;

	private HiddenAction hiddenAction;

	private AndAction andAction;

	private OrAction orAction;

	private AlternativeAction alternativeAction;

	private RenameAction renameAction;

	private ZoomInAction zoomIn;

	private ZoomOutAction zoomOut;

	private LegendAction legendAction;

	private LegendLayoutAction legendLayoutAction;

	private EditConstraintAction editConstraintAction;

	private CreateConstraintAction createConstraintAction;

	private ReverseOrderAction reverseOrderAction;

	public FeatureDiagramEditor(FeatureModelEditor featureModelEditor, Composite container) {
		this.featureModelEditor = featureModelEditor;
		
		graphicalViewer = new ScrollingGraphicalViewer();
		graphicalViewer.setKeyHandler(new GraphicalViewerKeyHandler(
				graphicalViewer));

		graphicalViewer.createControl(container);
		initializeGraphicalViewer();

		graphicalViewer.setEditDomain(new DefaultEditDomain(featureModelEditor));

		zoomManager = rootEditPart.getZoomManager();
		zoomManager.setZoomLevels(new double[] { 0.05, 0.10, 0.25, 0.50, 0.75,
				0.90, 1.00, 1.10, 1.25, 1.50, 2.00, 2.50, 3.00, 4.00 });
	}

	void initializeGraphicalViewer() {
		graphicalViewer.getControl().setBackground(DIAGRAM_BACKGROUND);
		graphicalViewer.setEditPartFactory(new GraphicalEditPartFactory());
		rootEditPart = new ScalableFreeformRootEditPart();
		((ConnectionLayer) rootEditPart
				.getLayer(LayerConstants.CONNECTION_LAYER))
				.setAntialias(SWT.ON);
		graphicalViewer.setRootEditPart(rootEditPart);
	}

	private FeatureModel getFeatureModel() {
		return featureModelEditor.getFeatureModel();
	}

	public Control getPage() {
		return graphicalViewer.getControl();
	}

	public void createActions() {
		FeatureModel featureModel = getFeatureModel();
		
		createLayerAction = new CreateLayerAction(graphicalViewer, featureModel);
		createCompoundAction = new CreateCompoundAction(graphicalViewer,
				featureModel);
		deleteAction = new DeleteAction(graphicalViewer, featureModel);
		mandatoryAction = new MandatoryAction(graphicalViewer, featureModel);
		hiddenAction = new HiddenAction(graphicalViewer, featureModel);
		abstractAction = new AbstractAction(graphicalViewer, featureModel,
				(ObjectUndoContext) featureModel.getUndoContext());
		andAction = new AndAction(graphicalViewer, featureModel);
		orAction = new OrAction(graphicalViewer, featureModel);
		alternativeAction = new AlternativeAction(graphicalViewer, featureModel);
		renameAction = new RenameAction(graphicalViewer, featureModel);
		
		createConstraintAction = new CreateConstraintAction(graphicalViewer,
				featureModel, "Create Constraint");
		editConstraintAction = new EditConstraintAction(graphicalViewer,
				featureModel, "Edit Constraint");
		reverseOrderAction = new ReverseOrderAction(graphicalViewer,
				featureModel);

		legendAction = new LegendAction(graphicalViewer, featureModel);
		legendLayoutAction = new LegendLayoutAction(graphicalViewer,
				featureModel);

		zoomIn = new ZoomInAction(zoomManager);
		zoomOut = new ZoomOutAction(zoomManager);
	}

	public void createContextMenu(MenuManager menu) {
		menu.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				FeatureDiagramEditor.this.fillContextMenu(manager);
			}
		});
		menu.createContextMenu(graphicalViewer.getControl());
		graphicalViewer.setContextMenu(menu);
		// the following line adds package explorer entries into our context
		// menu
		// getSite().registerContextMenu(menu, graphicalViewer);
	}

	public void createKeyBindings() {
		KeyHandler handler = graphicalViewer.getKeyHandler();
		handler.put(KeyStroke.getPressed(SWT.F2, 0), renameAction);
		handler.put(KeyStroke.getPressed(SWT.INSERT, 0), createLayerAction);
		graphicalViewer.setKeyHandler(handler);
	}

	private void fillContextMenu(IMenuManager menu) {
		if (andAction.isEnabled() || orAction.isEnabled()) {
			if (andAction.isChecked()) {
				andAction.setText("And");
				orAction.setText("Or (Double Click)");
				alternativeAction.setText("Alternative");
			} else if (orAction.isChecked()) {
				andAction.setText("And");
				orAction.setText("Or");
				alternativeAction.setText("Alternative (Double Click)");
			} else if (alternativeAction.isChecked()) {
				andAction.setText("And (Double Click)");
				orAction.setText("Or");
				alternativeAction.setText("Alternative");
			}
			menu.add(andAction);
			menu.add(orAction);
			menu.add(alternativeAction);
		} else if (createLayerAction.isEnabled()
				|| createCompoundAction.isEnabled()) {
			menu.add(createCompoundAction);
			menu.add(createLayerAction);
			menu.add(renameAction);
			menu.add(deleteAction);
			menu.add(new Separator());
			menu.add(mandatoryAction);
			menu.add(abstractAction);
			menu.add(hiddenAction);
			menu.add(new Separator());
			menu.add(reverseOrderAction);
		} else if (editConstraintAction.isEnabled()) {
			menu.add(createConstraintAction);
			menu.add(editConstraintAction);
			menu.add(deleteAction);
		} else {
			menu.add(createConstraintAction);
			menu.add(new Separator());
			menu.add(reverseOrderAction);
		}
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		menu.add(legendAction);
		if (legendLayoutAction.isEnabled())
			menu.add(legendLayoutAction);
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	public IAction getDiagramAction(String workbenchActionID) {
		if (CreateLayerAction.ID.equals(workbenchActionID))
			return createLayerAction;
		if (CreateCompoundAction.ID.equals(workbenchActionID))
			return createCompoundAction;
		if (DeleteAction.ID.equals(workbenchActionID))
			return deleteAction;
		if (MandatoryAction.ID.equals(workbenchActionID))
			return mandatoryAction;
		if (AbstractAction.ID.equals(workbenchActionID))
			return abstractAction;
		if (HiddenAction.ID.equals(workbenchActionID))
			return hiddenAction;
		if (AndAction.ID.equals(workbenchActionID))
			return andAction;
		if (OrAction.ID.equals(workbenchActionID))
			return orAction;
		if (AlternativeAction.ID.equals(workbenchActionID))
			return alternativeAction;
		if (RenameAction.ID.equals(workbenchActionID))
			return renameAction;
		if (GEFActionConstants.ZOOM_IN.equals(workbenchActionID))
			return zoomIn;
		if (GEFActionConstants.ZOOM_OUT.equals(workbenchActionID))
			return zoomOut;
		return null;
	}

	public void refresh() {
		if (graphicalViewer.getContents() == null)
			return;

		// refresh size of all feature figures
		graphicalViewer.getContents().refresh();
		// layout all features
		Point size = graphicalViewer.getControl().getSize();
		layoutManager.setControlSize(size.x, size.y);
		layoutManager.layout(getFeatureModel());

		// refresh position of all feature figures
		graphicalViewer.getContents().refresh();
	}

	public void refreshLegend() {
		legendAction.refresh();
		legendLayoutAction.refresh();
	}

	public GraphicalViewerImpl getGraphicalViewer() {
		return graphicalViewer;
	}

	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		if (GraphicalViewer.class.equals(adapter)
				|| EditPartViewer.class.equals(adapter))
			return graphicalViewer;
		if (ZoomManager.class.equals(adapter))
			return zoomManager;
		if (CommandStack.class.equals(adapter))
			return graphicalViewer.getEditDomain().getCommandStack();
		if (EditDomain.class.equals(adapter))
			return graphicalViewer.getEditDomain();
		return null;
	}

}
