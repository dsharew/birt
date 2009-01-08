/*******************************************************************************
 * Copyright (c) 2004 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Actuate Corporation  - initial API and implementation
 *******************************************************************************/

package org.eclipse.birt.report.model.core.namespace;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.birt.report.model.api.metadata.IElementDefn;
import org.eclipse.birt.report.model.core.DesignElement;
import org.eclipse.birt.report.model.core.Module;
import org.eclipse.birt.report.model.core.NameSpace;
import org.eclipse.birt.report.model.core.StyleElement;
import org.eclipse.birt.report.model.css.CssNameManager;
import org.eclipse.birt.report.model.css.CssStyle;
import org.eclipse.birt.report.model.elements.ICssStyleSheetOperation;
import org.eclipse.birt.report.model.elements.Library;
import org.eclipse.birt.report.model.elements.ReportDesign;
import org.eclipse.birt.report.model.elements.Style;
import org.eclipse.birt.report.model.elements.Theme;
import org.eclipse.birt.report.model.metadata.ElementRefValue;
import org.eclipse.birt.report.model.metadata.PropertyDefn;

/**
 * 
 */
public class StyleNameContext extends AbstractModuleNameContext
{

	/**
	 * Constructs one style element name space.
	 * 
	 * @param module
	 *            the attached module
	 */

	StyleNameContext( Module module )
	{
		super( module, Module.STYLE_NAME_SPACE );
	}

	/**
	 * Returns all elements in the module this module namespace is associated
	 * and those in the included modules. For the style name scope, the depth of
	 * the library is ignored.
	 * 
	 * @see org.eclipse.birt.report.model.core.namespace.IModuleNameScope#getElements(int)
	 */

	public List<DesignElement> getElements( int level )
	{
		Map<String, StyleElement> elements = new LinkedHashMap<String, StyleElement>( );

		Theme theme = module.getTheme( module );

		if ( theme == null && module instanceof Library )
		{
			return new ArrayList<DesignElement>( elements.values( ) );
		}

		if ( theme != null )
		{
			List<StyleElement> allStyles = theme.getAllStyles( );
			addAllStyles( elements, allStyles );
		}

		if ( module instanceof Library )
		{
			return new ArrayList<DesignElement>( elements.values( ) );
		}

		// find in css file

		List<CssStyle> csses = CssNameManager
				.getStyles( (ICssStyleSheetOperation) module );
		addAllStyles( elements, csses );

		// find all styles in report design.

		NameSpace ns = module.getNameHelper( ).getNameSpace( nameSpaceID );
		List<DesignElement> styles = ns.getElements( );
		addAllStyles( elements, styles );

		return new ArrayList<DesignElement>( elements.values( ) );
	}

	private void addAllStyles( Map<String, StyleElement> styleMap,
			List<? extends DesignElement> styleList )
	{
		assert styleMap != null;
		if ( styleList != null )
		{
			for ( int i = 0; i < styleList.size( ); i++ )
			{
				DesignElement style = styleList.get( i );
				styleMap.put( style.getName( ), (StyleElement) style );
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.birt.report.model.core.IModuleNameSpace#resolve(org.eclipse
	 * .birt.report.model.core.DesignElement)
	 */

	private ElementRefValue resolve( DesignElement element )
	{
		return new ElementRefValue( null, element );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.birt.report.model.core.IModuleNameSpace#resolve(java.lang
	 * .String)
	 */

	private ElementRefValue resolve( String elementName )
	{
		// this name is not cached, so find it directly
		Theme theme = module.getTheme( module );

		if ( theme == null && module instanceof Library )
		{
			return new ElementRefValue( null, elementName );
		}

		// find the style first in the report design first.

		DesignElement target = null;

		if ( module instanceof ReportDesign )
		{
			NameSpace ns = module.getNameHelper( ).getNameSpace( nameSpaceID );
			target = ns.getElement( elementName );

			if ( target != null )
			{
				return new ElementRefValue( null, target );
			}

			// find in css file

			List<CssStyle> csses = CssNameManager
					.getStyles( (ICssStyleSheetOperation) module );
			for ( int i = 0; csses != null && i < csses.size( ); ++i )
			{
				CssStyle s = csses.get( i );
				if ( elementName.equalsIgnoreCase( s.getFullName( ) ) )
				{
					return new ElementRefValue( null, s );
				}
			}
		}

		// find the style in the library.

		DesignElement libraryStyle = null;
		if ( theme != null )
		{
			libraryStyle = theme.findStyle( elementName );
		}

		if ( libraryStyle != null )
		{
			return new ElementRefValue( null, libraryStyle );
		}

		// find style in toc default style
		
		List<DesignElement> defaultTocStyle = module.getSession( )
				.getDefaultTOCStyleValue( );
		Iterator<DesignElement> iterator = defaultTocStyle.iterator( );
		while ( iterator.hasNext( ) )
		{
			Style tmpStyle = (Style) iterator.next( );
			if ( tmpStyle.getName( ).equalsIgnoreCase( elementName ) )
			{
				return new ElementRefValue( null, tmpStyle );
			}
		}
		// if the style is not find, return a unresolved element reference
		// value.

		return new ElementRefValue( null, elementName );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.birt.report.model.core.namespace.AbstractNameScope#resolve
	 * (org.eclipse.birt.report.model.core.DesignElement,
	 * org.eclipse.birt.report.model.metadata.PropertyDefn)
	 */

	public ElementRefValue resolve( DesignElement element, PropertyDefn propDefn )
	{
		return resolve( element );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.birt.report.model.core.namespace.AbstractNameScope#resolve
	 * (java.lang.String, org.eclipse.birt.report.model.metadata.PropertyDefn)
	 */

	public ElementRefValue resolve( String elementName, PropertyDefn propDefn )
	{
		return resolve( elementName );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.birt.report.model.core.namespace.INameContext#findElement
	 * (java.lang.String,
	 * org.eclipse.birt.report.model.api.metadata.IElementDefn)
	 */
	public DesignElement findElement( String elementName,
			IElementDefn elementDefn )
	{
		ElementRefValue refValue = resolve( elementName );
		return refValue == null ? null : refValue.getElement( );
	}
}
