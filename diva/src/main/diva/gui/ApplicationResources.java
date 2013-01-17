/*
 * $Id: ApplicationResources.java,v 1.9 2002/04/18 02:31:20 cxh Exp $
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.gui;

import java.awt.Toolkit;
import java.awt.Image;

import java.net.URL;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;

/**
 * A class for helping to manage application resources. This class is
 * an extension to the standard ResourceBundle that allows to
 * construct ResourceBundles that "override" other resource
 * bundles. Thus, an abstract application class can have a set of
 * default resources, and a particular application subclass can add
 * its own resources to override the defaults where appropriate.
 *
 * @deprecated Use diva.resource.DefaultBundle instead.
 * @author John Reekie (johnr@eecs.berkeley.edu)
 * @version $Revision: 1.9 $
 */
public class ApplicationResources extends ResourceBundle {

    /** The class that is to be used to look up URL resources
     * from this bundle.
     */
    private Class _class;

    /** Icons that have already been loaded
     */
    private HashMap _imageIcons = new HashMap();

    /** The actual ResourceBundle
     */
    private ResourceBundle _delegate;

    /** Create a new ApplicationResources object containing
     * the default Diva GUI resources.
     */
    public ApplicationResources () {
        this("diva.resource.Defaults", null, null);
        _class = getClass();
    }

    /** Create a new ApplicationResources object using the given basename,
     * with the given class as the loader for URL-based resources, and with the
     * given ResourceBundle as the one that gets overridden.
     */
    public ApplicationResources (String baseName, Class withLoader,
            ResourceBundle overrides) {
        try {
            _delegate = ResourceBundle.getBundle(baseName, Locale.getDefault());
        } catch (MissingResourceException e) {
            System.err.println( baseName + ".properties not found");
            System.exit(1);
        }
        _class = getClass();
        if (overrides != null) {
            setParent(overrides);
        }
    }

    /** Get a resource as an absolute URL.
     */
    public URL getResource (String key) {
        String s = getString(key);
	// Web Start requires using getClassLoader.
        return _class.getClassLoader().getResource(s);        
    }

    /** Get a resource as an image icon. The name of the resource is
     * formed by appending "Image" to the given key. Return null if not found.
     * (Or should this throw an exception?)
     */
    public ImageIcon getImageIcon (String key) {
        ImageIcon icon = (ImageIcon)_imageIcons.get(key + "Image");
        if (icon == null) {
            URL url = getResource(key + "Image");
            if (url != null) {
                icon = new ImageIcon(url);
                _imageIcons.put(key+"Image", icon);
            }
        }
        return icon;
    }

    /** Get a resource as an image. The name of the resource is
     * formed by appending "Image" to the given key. Return null if not found.
     * (Or should this throw an exception?)
     */
    public Image getImage (String key) {
        URL url = getResource(key + "Image");
        
        if (url != null) {
            Toolkit tk = Toolkit.getDefaultToolkit();
            Image img = tk.getImage(url);
            return img;
        }
        return null;
    }
    
    /** Get an object from a ResourceBundle.
     */
    protected Object handleGetObject(String key)
            throws MissingResourceException {
        return _delegate.getObject(key);
    }

    /** Get an enumeration over the keys
     */
    public Enumeration getKeys () {
        return _delegate.getKeys();
    }
}


