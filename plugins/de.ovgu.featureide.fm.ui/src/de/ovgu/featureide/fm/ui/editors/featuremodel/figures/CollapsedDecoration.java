/* FeatureIDE - A Framework for Feature-Oriented Software Development
 * Copyright (C) 2005-2016  FeatureIDE team, University of Magdeburg, Germany
 *
 * This file is part of FeatureIDE.
 * 
 * FeatureIDE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * FeatureIDE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with FeatureIDE.  If not, see <http://www.gnu.org/licenses/>.
 *
 * See http://featureide.cs.ovgu.de/ for further information.
 */
package de.ovgu.featureide.fm.ui.editors.featuremodel.figures;

import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.RotatableDecoration;
import org.eclipse.draw2d.Shape;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.w3c.dom.css.Rect;

import de.ovgu.featureide.fm.core.base.IFeatureStructure;
import de.ovgu.featureide.fm.ui.FMUIPlugin;
import de.ovgu.featureide.fm.ui.editors.IGraphicalFeature;
import de.ovgu.featureide.fm.ui.editors.featuremodel.GUIDefaults;
import de.ovgu.featureide.fm.ui.properties.FMPropertyManager;

/**
 * A decoration for a feature connection that indicates the mandatory property.
 * 
 * @author Joshua Sprey
 * @author Enis Belli
 * @author Christopher Sontag
 * @author Maximilian Kühl
 */
public class CollapsedDecoration extends Shape implements RotatableDecoration, GUIDefaults {
	private static int counter = 0;
	private final Label childrenCount = new Label();
	//	private static GridLayout gl = new GridLayout();
	private static final FreeformLayout layout = new FreeformLayout();
	public static final boolean drawConnenctionLines = true;

	private IGraphicalFeature graphicalFeature;

	public CollapsedDecoration(IGraphicalFeature parent) {
		super();
		graphicalFeature = parent;
		setLayoutManager(layout);
		setBackgroundColor(FMPropertyManager.getConcreteFeatureBackgroundColor());
		//setBorder(FMPropertyManager.getFeatureBorder(false));

		childrenCount.setFont(DEFAULT_FONT);
		setDecoratorText("" + GetAllChildren(parent.getObject().getStructure()));
		add(childrenCount);
	}
	
	public CollapsedDecoration() {
		super();
		setLayoutManager(layout);
		setBackgroundColor(FMPropertyManager.getConcreteFeatureBackgroundColor());
		setBorder(FMPropertyManager.getFeatureBorder(false));

		childrenCount.setFont(DEFAULT_FONT);
		setDecoratorText("n");
		add(childrenCount);
	}

	@Override
	public void setLocation(Point p) {
		if (graphicalFeature != null)
			if (graphicalFeature.getGraphicalModel().getLayout().getLayoutAlgorithm() == 4) {
			//left to right layout 
			super.setLocation(p.translate(GUIDefaults.COLLAPSED_DECORATOR_FEATURE_SPACE, -getBounds().height / 2));
			}
		super.setLocation(p.translate(-(getBounds().width / 2), GUIDefaults.COLLAPSED_DECORATOR_FEATURE_SPACE));
	}
	
	public int GetAllChildren(IFeatureStructure parent)
	{
		int count = 0;
		for (IFeatureStructure iterable_element : parent.getChildren()) {
			count += 1 + GetAllChildren(iterable_element);			
		}
		return count;
	}

	public void setDecoratorText(String newText) {
		if (childrenCount.getText().equals(newText)) {
			return;
		}
		childrenCount.setText(newText);

		final Dimension labelSize = childrenCount.getPreferredSize();
		this.minSize = labelSize;

		if (!labelSize.equals(childrenCount.getSize())) {
			childrenCount.setBounds(
					new Rectangle(GUIDefaults.COLLAPSED_DECORATOR_X_SPACE, GUIDefaults.COLLAPSED_DECORATOR_Y_SPACE, labelSize.width, labelSize.height));

			final Rectangle bounds = getBounds();
			labelSize.width += GUIDefaults.COLLAPSED_DECORATOR_X_SPACE * 2;
			labelSize.height += GUIDefaults.COLLAPSED_DECORATOR_Y_SPACE * 2;
			bounds.setSize(labelSize);

			final Dimension oldSize = getSize();
			if (!oldSize.equals(0, 0)) {
				bounds.x += (oldSize.width - bounds.width) >> 1;
			}
			//			setBounds(bounds);
			bounds.height += 30;
			setBounds(bounds);

		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.draw2d.RotatableDecoration#setReferencePoint(org.eclipse.draw2d.geometry.Point)
	 */
	@Override
	public void setReferencePoint(Point p) {

	}

	/* (non-Javadoc)
	 * @see org.eclipse.draw2d.Shape#fillShape(org.eclipse.draw2d.Graphics)
	 */
	@Override
	protected void fillShape(Graphics graphics) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.draw2d.Shape#outlineShape(org.eclipse.draw2d.Graphics)
	 */
	@Override
	protected void outlineShape(Graphics graphics) {
		int x = getBounds().x + 1;
		int y = getBounds().y + 1;
		int width = getBounds().width - 2;
		if (width % 2 == 1) {
			width += 1;
			setBounds(new Rectangle(getBounds().x, getBounds().y, getBounds().width + 1, getBounds().height));
		}
		int height = getBounds().height - 32;
		graphics.setLineWidth(1);
		graphics.setForegroundColor(FMPropertyManager.getFeatureBorderColor());
		graphics.drawRoundRectangle(new Rectangle(x, y, width, height), GUIDefaults.COLLAPSED_DECORATOR_ARC_RADIUS, GUIDefaults.COLLAPSED_DECORATOR_ARC_RADIUS);

		if (drawConnenctionLines) {
			graphics.setLineWidth(1);
			graphics.setLineStyle(SWT.LINE_DASH);
			int childrenCount = graphicalFeature.getObject().getStructure().getChildrenCount();
			Point origin = new Point(getBounds().x + width / 2 + 1, getBounds().y + height + 1);
			if (childrenCount != 0) {
				double angle = 90 / (double) (childrenCount + 1);
				for (int i = 0; i < childrenCount; i++) {
					double ownAngle = angle * (i + 1);
					Point target = getPoint(ownAngle, origin);
					graphics.setLineWidth(1);
					graphics.setLineStyle(SWT.LINE_SOLID);
					graphics.drawOval(new Rectangle(new Point(target.x - 1, target.y - 1), new Dimension(2, 2)));
					graphics.setLineWidth(1);
					graphics.setLineStyle(SWT.LINE_DASH);
					graphics.drawLine(origin, target);
				}
			}
			graphics.setLineStyle(SWT.LINE_SOLID);
		}
	}

	private Point getPoint(double gamma, Point origin) {
		boolean appendOffsetRight = false;
		if (gamma > 45) {
			appendOffsetRight = true;
			gamma -= 45;
		} else {
			gamma = 45 - gamma;
		}
		double alpha = 90 - gamma;
		double b = 15;
		double a = Math.sin(Math.toRadians(alpha)) * b;
		double c = Math.cos(Math.toRadians(alpha)) * b;
		if (appendOffsetRight) {
			return new Point((int) (origin.x + Math.abs(c)), (int) (origin.y + Math.abs(a)));
		} else {
			return new Point((int) (origin.x - Math.abs(c)), (int) (origin.y + Math.abs(a)));
		}
	}

}
