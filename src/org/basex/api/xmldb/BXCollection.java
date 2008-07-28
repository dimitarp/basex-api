package org.basex.api.xmldb;

import org.xmldb.api.base.*;
import org.basex.core.Context;

/**
 * Implementation of the Collection Interface for the XMLDB:API
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Andreas Weiler
 */
public class BXCollection implements Collection {
  
  /** Context ctx */
  Context ctx;
  
  /**
   * Standard constructor.
   * @param ctx for Context
   */
  public BXCollection(Context ctx) {
    this.ctx = ctx;
  }

  /**
   * @see org.xmldb.api.base.Collection#close()
   */
  public void close() {
    // TODO Auto-generated method stub
    
  }

  /**
   * @see org.xmldb.api.base.Collection#createId()
   */
  public String createId() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see org.xmldb.api.base.Collection#createResource(java.lang.String, java.lang.String)
   */
  public Resource createResource(String id, String type) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see org.xmldb.api.base.Collection#getChildCollection(java.lang.String)
   */
  public Collection getChildCollection(String name) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see org.xmldb.api.base.Collection#getChildCollectionCount()
   */
  public int getChildCollectionCount() {
    // TODO Auto-generated method stub
    return 0;
  }

  /**
   * @see org.xmldb.api.base.Collection#getName()
   */
  public String getName() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see org.xmldb.api.base.Collection#getParentCollection()
   */
  public Collection getParentCollection() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see org.xmldb.api.base.Configurable#getProperty(java.lang.String)
   */
  public String getProperty(String name) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see org.xmldb.api.base.Collection#getResource(java.lang.String)
   */
  public Resource getResource(String id) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see org.xmldb.api.base.Collection#getResourceCount()
   */
  public int getResourceCount() {
    // TODO Auto-generated method stub
    return 0;
  }

  /**
   * @see org.xmldb.api.base.Collection#getService(java.lang.String, java.lang.String)
   */
  public Service getService(String name, String version) {
    if(name.startsWith("XPath")) {
    return new BXXPathQueryService(ctx);
    } 
    return null;
  }

  /**
   * @see org.xmldb.api.base.Collection#getServices()
   */
  public Service[] getServices() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see org.xmldb.api.base.Collection#isOpen()
   */
  public boolean isOpen() {
    // TODO Auto-generated method stub
    return false;
  }

  /**
   * @see org.xmldb.api.base.Collection#listChildCollections()
   */
  public String[] listChildCollections() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see org.xmldb.api.base.Collection#listResources()
   */
  public String[] listResources() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see org.xmldb.api.base.Collection#removeResource(org.xmldb.api.base.Resource)
   */
  public void removeResource(Resource res) {
    // TODO Auto-generated method stub
    
  }

  /**
   * @see org.xmldb.api.base.Configurable#setProperty(java.lang.String, java.lang.String)
   */
  public void setProperty(String name, String value) {
    // TODO Auto-generated method stub
    
  }

  /**
   * @see org.xmldb.api.base.Collection#storeResource(org.xmldb.api.base.Resource)
   */
  public void storeResource(Resource res) {
    // TODO Auto-generated method stub
    
  }
}