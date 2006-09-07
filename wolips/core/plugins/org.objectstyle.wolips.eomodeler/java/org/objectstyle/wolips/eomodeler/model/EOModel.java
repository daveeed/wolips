/*
 * ====================================================================
 * 
 * The ObjectStyle Group Software License, Version 1.0
 * 
 * Copyright (c) 2006 The ObjectStyle Group and individual authors of the
 * software. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 * include the following acknowlegement: "This product includes software
 * developed by the ObjectStyle Group (http://objectstyle.org/)." Alternately,
 * this acknowlegement may appear in the software itself, if and wherever such
 * third-party acknowlegements normally appear.
 * 
 * 4. The names "ObjectStyle Group" and "Cayenne" must not be used to endorse or
 * promote products derived from this software without prior written permission.
 * For written permission, please contact andrus@objectstyle.org.
 * 
 * 5. Products derived from this software may not be called "ObjectStyle" nor
 * may "ObjectStyle" appear in their names without prior written permission of
 * the ObjectStyle Group.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * OBJECTSTYLE GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 * 
 * This software consists of voluntary contributions made by many individuals on
 * behalf of the ObjectStyle Group. For more information on the ObjectStyle
 * Group, please see <http://objectstyle.org/>.
 *  
 */
package org.objectstyle.wolips.eomodeler.model;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.objectstyle.cayenne.wocompat.PropertyListSerialization;
import org.objectstyle.wolips.eomodeler.utils.ComparisonUtils;
import org.objectstyle.wolips.eomodeler.utils.NotificationMap;

public class EOModel extends UserInfoableEOModelObject implements IUserInfoable, ISortableEOModelObject, IConnectionDictionaryOwner {
  public static final String ENTITY_MODELER_KEY = "_EntityModeler";
  public static final String DIRTY = "dirty";
  public static final String ENTITY = "entity";
  public static final String ADAPTOR_NAME = "adaptorName";
  public static final String VERSION = "version";
  public static final String NAME = "name";
  public static final String ENTITIES = "entities";
  public static final String STORED_PROCEDURE = "storedProcedure";
  public static final String STORED_PROCEDURES = "storedProcedures";
  public static final String DATABASE_CONFIG = "databaseConfig";
  public static final String DATABASE_CONFIGS = "databaseConfigs";

  private EOModelGroup myModelGroup;
  private String myName;
  private String myVersion;
  private String myAdaptorName;
  private NotificationMap myConnectionDictionary;
  private Set myEntities;
  private Set myDatabaseConfigs;
  private Set myStoredProcedures;
  private Set myDeletedEntityNamesInObjectStore;
  private Set myDeletedEntityNames;
  private Set myDeletedStoredProcedureNames;
  private EOModelMap myModelMap;
  private boolean myDirty;
  private PropertyChangeRepeater myConnectionDictionaryRepeater;
  private File myModelFolder;
  private Set myPrototypeAttributeCache;

  public EOModel(String _name) {
    myName = _name;
    myEntities = new HashSet();
    myStoredProcedures = new HashSet();
    myDeletedEntityNamesInObjectStore = new TreeSet();
    myDeletedEntityNames = new TreeSet();
    myDeletedStoredProcedureNames = new TreeSet();
    myDatabaseConfigs = new HashSet();
    myVersion = "2.1";
    myModelMap = new EOModelMap();
    myConnectionDictionaryRepeater = new PropertyChangeRepeater(IConnectionDictionaryOwner.CONNECTION_DICTIONARY);
    setConnectionDictionary(new NotificationMap(), false);
  }

  protected void _storedProcedureChanged(EOStoredProcedure _storedProcedure, String _propertyName, Object _oldValue, Object _newValue) {
    firePropertyChange(EOModel.STORED_PROCEDURE, null, _storedProcedure);
  }

  public void _setModelGroup(EOModelGroup _modelGroup) {
    myModelGroup = _modelGroup;
  }

  public Set getReferenceFailures() {
    return new HashSet();
  }
  
  public String guessPackageName() {
	  return guessPackageName(getEntities());
  }
  
  public String guessPackageName(Set _entities) {
	  String guessPackageName = null;
	  Iterator entitiesIter = _entities.iterator();
	  while (entitiesIter.hasNext()) {
		  EOEntity entity = (EOEntity)entitiesIter.next();
		  String className = entity.getClassName();
		  if (className != null) {
			  int packageNameEnd = className.lastIndexOf('.');
			  String packageName;
			  if (packageNameEnd != -1) {
				  packageName = className.substring(0, packageNameEnd);
			  }
			  else {
				  packageName = "";
			  }
			  if (guessPackageName == null) {
				  guessPackageName = packageName;
			  }
			  else if ("".equals(guessPackageName)) {
				  // it can't change from ""
			  }
			  else if (!guessPackageName.equals(packageName)) {
				  if (guessPackageName.startsWith(packageName)) {
					  guessPackageName = packageName;
				  }
				  else if (packageName.startsWith(guessPackageName)) {
					  // leave it as is
				  }
				  else {
					  int lastMatchingIndex = -1;
					  for (int index = 0; index < guessPackageName.length() && index < packageName.length(); index++) {
						if (guessPackageName.charAt(index) == packageName.charAt(index)) {
							lastMatchingIndex = index;
						}
						else {
							break;
						}
					  }
					  if (lastMatchingIndex != -1) {
						  guessPackageName = guessPackageName.substring(0, lastMatchingIndex);
						  if (guessPackageName.endsWith(".")) {
							  guessPackageName = guessPackageName.substring(0, guessPackageName.length() - 1);
						  }
					  }
					  else {
						  guessPackageName = "";
					  }
				  }
			  }
		  }
	  }
	  return guessPackageName;
  }

  public EOEntity addBlankEntity(String _name) throws DuplicateNameException {
    String newEntityNameBase = _name;
    String newEntityName = newEntityNameBase;
    int newEntityNum = 0;
    while (getEntityNamed(newEntityName) != null) {
      newEntityNum++;
      newEntityName = newEntityNameBase + newEntityNum;
    }
    EOEntity entity = new EOEntity(newEntityName);
    entity.setExternalName(newEntityName);
    String className = newEntityName;
    String packageName = guessPackageName();
    if (packageName != null && packageName.length() > 0) {
    	className = packageName + "." + newEntityName;
    }
    entity.setClassName(className);
    addEntity(entity);
    return entity;
  }

  public boolean isDirty() {
    return myDirty;
  }

  public void setDirty(boolean _dirty) {
    Boolean oldDirty = Boolean.valueOf(myDirty);
    myDirty = _dirty;
    firePropertyChange(EOModel.DIRTY, oldDirty, Boolean.valueOf(myDirty));
  }

  protected void firePropertyChange(String _propertyName, Object _oldValue, Object _newValue) {
    super.firePropertyChange(_propertyName, _oldValue, _newValue);
  }

  protected void _propertyChanged(String _propertyName, Object _oldValue, Object _newValue) {
    if (!myDirty && !EOModel.DIRTY.equals(_propertyName)) {
      setDirty(true);
    }
    if (_propertyName == IConnectionDictionaryOwner.CONNECTION_DICTIONARY) {
      clearCachedPrototypes(null, false);
    }
  }

  protected void _entityChanged(EOEntity _entity, String _propertyName, Object _oldValue, Object _newValue) {
    firePropertyChange(EOModel.ENTITY, null, _entity);
  }

  protected void _databaseConfigChanged(EODatabaseConfig _databaseConfig, String _propertyName, Object _oldValue, Object _newValue) {
    firePropertyChange(EOModel.DATABASE_CONFIG, null, _databaseConfig);
  }

  public EODatabaseConfig getDatabaseConfigNamed(String _name) {
    EODatabaseConfig matchingDatabaseConfig = null;
    if (EODatabaseConfig.DEFAULT_NAME.equals(_name)) {
      matchingDatabaseConfig = createDefaultDatabaseConfig();
    }
    else {
      Iterator databaseConfigsIter = myDatabaseConfigs.iterator();
      while (matchingDatabaseConfig == null && databaseConfigsIter.hasNext()) {
        EODatabaseConfig entity = (EODatabaseConfig) databaseConfigsIter.next();
        if (ComparisonUtils.equals(entity.getName(), _name)) {
          matchingDatabaseConfig = entity;
        }
      }
    }
    return matchingDatabaseConfig;
  }

  public String findUnusedDatabaseConfigName(String _newName) {
    String unusedName = _newName;
    boolean unusedNameFound = (getDatabaseConfigNamed(_newName) == null);
    for (int dupeNameNum = 1; !unusedNameFound; dupeNameNum++) {
      unusedName = _newName + dupeNameNum;
      EODatabaseConfig renameDatabaseConfig = getDatabaseConfigNamed(unusedName);
      unusedNameFound = (renameDatabaseConfig == null);
    }
    return unusedName;
  }

  public void _checkForDuplicateDatabaseConfigName(EODatabaseConfig _databaseConfig, String _newName, Set _failures) throws DuplicateDatabaseConfigNameException {
    EODatabaseConfig existingDatabaseConfig = getDatabaseConfigNamed(_newName);
    if (existingDatabaseConfig != null && existingDatabaseConfig != _databaseConfig) {
      if (_failures == null) {
        throw new DuplicateDatabaseConfigNameException(_newName, this);
      }
      String unusedName = findUnusedDatabaseConfigName(_newName);
      existingDatabaseConfig.setName(unusedName, true);
      _failures.add(new DuplicateDatabaseConfigFailure(this, _newName, unusedName));
    }
  }

  public EODatabaseConfig addBlankDatabaseConfig(String _name) throws DuplicateNameException {
    String newDatabaseConfigNameBase = _name;
    String newDatabaseConfigName = findUnusedDatabaseConfigName(newDatabaseConfigNameBase);
    EODatabaseConfig databaseConfig = new EODatabaseConfig(newDatabaseConfigName);
    addDatabaseConfig(databaseConfig);
    return databaseConfig;
  }

  public void addDatabaseConfig(EODatabaseConfig _databaseConfig) throws DuplicateNameException {
    addDatabaseConfig(_databaseConfig, true, null);
  }

  public void addDatabaseConfig(EODatabaseConfig _databaseConfig, boolean _fireEvents, Set _failures) throws DuplicateNameException {
    _databaseConfig._setModel(this);
    _checkForDuplicateDatabaseConfigName(_databaseConfig, _databaseConfig.getName(), _failures);
    _databaseConfig.pasted();
    if (_fireEvents) {
      Set oldDatabaseConfigs = null;
      oldDatabaseConfigs = myDatabaseConfigs;
      Set newEntities = new HashSet();
      newEntities.addAll(myDatabaseConfigs);
      newEntities.add(_databaseConfig);
      myDatabaseConfigs = newEntities;
      firePropertyChange(EOModel.DATABASE_CONFIGS, oldDatabaseConfigs, myDatabaseConfigs);
    }
    else {
      myDatabaseConfigs.add(_databaseConfig);
    }
  }

  public void removeDatabaseConfig(EODatabaseConfig _databaseConfig) {
    Set oldDatabaseConfigs = myDatabaseConfigs;
    Set newDatabaseConfigs = new HashSet();
    newDatabaseConfigs.addAll(myDatabaseConfigs);
    newDatabaseConfigs.remove(_databaseConfig);
    myDatabaseConfigs = newDatabaseConfigs;
    firePropertyChange(EOModel.DATABASE_CONFIGS, oldDatabaseConfigs, myDatabaseConfigs);
    _databaseConfig._setModel(null);
  }

  public EODatabaseConfig createDefaultDatabaseConfig() {
    EODatabaseConfig defaultDatabaseConfig = new EODatabaseConfig(EODatabaseConfig.DEFAULT_NAME);
    defaultDatabaseConfig.setConnectionDictionary(new HashMap(myConnectionDictionary));
    defaultDatabaseConfig.setPrototype(getPreferredPrototypeEntity());
    defaultDatabaseConfig._setModel(this);
    return defaultDatabaseConfig;
  }

  public Set getDatabaseConfigs(boolean _includeDefault) {
    Set databaseConfigs = new LinkedHashSet();
    if (_includeDefault) {
      databaseConfigs.add(createDefaultDatabaseConfig());
    }
    databaseConfigs.addAll(myDatabaseConfigs);
    return databaseConfigs;
  }

  public int hashCode() {
    return myName.hashCode();
  }

  public boolean equals(Object _obj) {
    return (_obj instanceof EOModel && ((EOModel) _obj).myName.equals(myName));
  }

  public File getModelFolder() {
    return myModelFolder;
  }

  public void setModelFolder(File _modelFolder) {
    myModelFolder = _modelFolder;
  }

  public EOModelGroup getModelGroup() {
    return myModelGroup;
  }

  public String getAdaptorName() {
    return myAdaptorName;
  }

  public void setAdaptorName(String _adaptorName) {
    String oldAdaptorName = myAdaptorName;
    myAdaptorName = _adaptorName;
    firePropertyChange(EOModel.ADAPTOR_NAME, _adaptorName, oldAdaptorName);
  }

  public String getVersion() {
    return myVersion;
  }

  public void setVersion(String _version) {
    String oldVersion = myVersion;
    myVersion = _version;
    firePropertyChange(EOModel.VERSION, _version, oldVersion);
  }

  public void setName(String _name) {
    String oldName = myName;
    myName = _name;
    firePropertyChange(EOModel.NAME, _name, oldName);
  }

  public String getName() {
    return myName;
  }

  public Set getEntities() {
    return myEntities;
  }

  public String findUnusedEntityName(String _newName) {
    String unusedName = _newName;
    boolean unusedNameFound = (myModelGroup.getEntityNamed(_newName) == null && getEntityNamed(_newName) == null);
    for (int dupeNameNum = 1; !unusedNameFound; dupeNameNum++) {
      unusedName = _newName + dupeNameNum;
      EOEntity renameEntity = myModelGroup.getEntityNamed(unusedName);
      if (renameEntity == null) {
        renameEntity = getEntityNamed(unusedName);
      }
      unusedNameFound = (renameEntity == null);
    }
    return unusedName;
  }

  public void _checkForDuplicateEntityName(EOEntity _entity, String _newName, Set _failures) throws DuplicateEntityNameException {
    EOEntity existingEntity = null;
    if (myModelGroup != null) {
      existingEntity = myModelGroup.getEntityNamed(_newName);
    }
    // MS: We do this because at load time, the model thinks it's part of the model group, but the model group doesnt'
    // think it contains the model, so we check both
    if (existingEntity == null) {
      existingEntity = getEntityNamed(_newName);
    }
    if (existingEntity != null && existingEntity != _entity) {
      // MS: For most duplicates, we can rename the original.  But for entities, they can be
      // in a totally separate model.
      if (_failures == null || _entity.getModel() != existingEntity.getModel()) {
        throw new DuplicateEntityNameException(_newName, this);
      }
      String unusedName = findUnusedEntityName(_newName);
      existingEntity.setName(unusedName, true);
      _failures.add(new DuplicateEntityFailure(this, _newName, unusedName));
    }
  }

  public void _entityNameChanged(String _oldName, String _newName) {
    if (myDeletedEntityNamesInObjectStore == null) {
      myDeletedEntityNamesInObjectStore = new TreeSet();
    }
    myDeletedEntityNamesInObjectStore.add(_oldName);
    myDeletedEntityNamesInObjectStore.remove(_newName);
    myDeletedEntityNames.add(_oldName);
    myDeletedEntityNames.remove(_newName);
  }

  public boolean containsEntityNamed(String _entityName) {
    return getEntityNamed(_entityName) != null;
  }

  public void addEntity(EOEntity _entity) throws DuplicateNameException {
    addEntity(_entity, true, null);
  }

  public void addEntity(EOEntity _entity, boolean _fireEvents, Set _failures) throws DuplicateNameException {
    _entity._setModel(this);
    _checkForDuplicateEntityName(_entity, _entity.getName(), _failures);
    _entity.pasted();
    myDeletedEntityNames.remove(_entity.getName());
    if (_fireEvents) {
      Set oldEntities = null;
      oldEntities = myEntities;
      Set newEntities = new HashSet();
      newEntities.addAll(myEntities);
      newEntities.add(_entity);
      myEntities = newEntities;
      firePropertyChange(EOModel.ENTITIES, oldEntities, myEntities);
    }
    else {
      myEntities.add(_entity);
    }
  }

  public void removeEntity(EOEntity _entity) {
    myDeletedEntityNames.add(_entity.getName());
    Set oldEntities = myEntities;
    Set newEntities = new HashSet();
    newEntities.addAll(myEntities);
    newEntities.remove(_entity);
    myEntities = newEntities;
    firePropertyChange(EOModel.ENTITIES, oldEntities, myEntities);
    _entity._setModel(null);
  }

  public EOEntity getEntityNamed(String _name) {
    EOEntity matchingEntity = null;
    Iterator entitiesIter = myEntities.iterator();
    while (matchingEntity == null && entitiesIter.hasNext()) {
      EOEntity entity = (EOEntity) entitiesIter.next();
      if (ComparisonUtils.equals(entity.getName(), _name)) {
        matchingEntity = entity;
      }
    }
    return matchingEntity;
  }

  public void _storedProcedureNameChanged(String _oldName, String _newName) {
    myDeletedStoredProcedureNames.add(_oldName);
    myDeletedStoredProcedureNames.remove(_newName);
  }

  public EOStoredProcedure addBlankStoredProcedure(String _name) throws DuplicateStoredProcedureNameException {
    String newStoredProcedureNameBase = _name;
    String newStoredProcedureName = findUnusedStoredProcedureName(newStoredProcedureNameBase);
    EOStoredProcedure storedProcedure = new EOStoredProcedure(newStoredProcedureName);
    addStoredProcedure(storedProcedure);
    return storedProcedure;
  }

  public void addStoredProcedure(EOStoredProcedure _storedProcedure) throws DuplicateStoredProcedureNameException {
    addStoredProcedure(_storedProcedure, true, null);
  }

  public void addStoredProcedure(EOStoredProcedure _storedProcedure, boolean _fireEvents, Set _failures) throws DuplicateStoredProcedureNameException {
    _storedProcedure._setModel(this);
    _checkForDuplicateStoredProcedureName(_storedProcedure, _storedProcedure.getName(), _failures);
    _storedProcedure.pasted();
    myDeletedStoredProcedureNames.remove(_storedProcedure.getName());
    if (_fireEvents) {
      Set oldStoredProcedures = myStoredProcedures;
      Set newStoredProcedures = new HashSet();
      newStoredProcedures.addAll(myStoredProcedures);
      newStoredProcedures.add(_storedProcedure);
      myStoredProcedures = newStoredProcedures;
      firePropertyChange(EOModel.STORED_PROCEDURES, oldStoredProcedures, myStoredProcedures);
    }
    else {
      myStoredProcedures.add(_storedProcedure);
    }
  }

  public void removeStoredProcedure(EOStoredProcedure _storedProcedure) {
    myDeletedStoredProcedureNames.add(_storedProcedure.getName());
    Set oldStoredProcedures = myStoredProcedures;
    Set newStoredProcedures = new HashSet();
    newStoredProcedures.addAll(myStoredProcedures);
    newStoredProcedures.remove(_storedProcedure);
    myStoredProcedures = newStoredProcedures;
    firePropertyChange(EOModel.STORED_PROCEDURES, oldStoredProcedures, myStoredProcedures);
    _storedProcedure._setModel(null);
  }

  public String findUnusedStoredProcedureName(String _newName) {
    boolean unusedNameFound = (getStoredProcedureNamed(_newName) == null);
    String unusedName = _newName;
    for (int dupeNameNum = 1; !unusedNameFound; dupeNameNum++) {
      unusedName = _newName + dupeNameNum;
      EOStoredProcedure renameStoredProcedure = getStoredProcedureNamed(unusedName);
      unusedNameFound = (renameStoredProcedure == null);
    }
    return unusedName;
  }

  public Set getStoredProcedures() {
    return myStoredProcedures;
  }

  public EOStoredProcedure getStoredProcedureNamed(String _name) {
    EOStoredProcedure matchingStoredProcedure = null;
    Iterator storedProceduresIter = myStoredProcedures.iterator();
    while (matchingStoredProcedure == null && storedProceduresIter.hasNext()) {
      EOStoredProcedure attribute = (EOStoredProcedure) storedProceduresIter.next();
      if (ComparisonUtils.equals(attribute.getName(), _name)) {
        matchingStoredProcedure = attribute;
      }
    }
    return matchingStoredProcedure;
  }

  public void _checkForDuplicateStoredProcedureName(EOStoredProcedure _storedProcedure, String _newName, Set _failures) throws DuplicateStoredProcedureNameException {
    EOStoredProcedure existingStoredProcedure = getStoredProcedureNamed(_newName);
    if (existingStoredProcedure != null && existingStoredProcedure != _storedProcedure) {
      if (_failures == null) {
        throw new DuplicateStoredProcedureNameException(_newName, this);
      }

      String unusedName = findUnusedStoredProcedureName(_newName);
      existingStoredProcedure.setName(unusedName, true);
      _failures.add(new DuplicateStoredProcedureFailure(this, _newName, unusedName));
    }
  }

  public void setUsername(String _userName) {
    getConnectionDictionary().put("username", _userName);
  }

  public String getUsername() {
    return (String) getConnectionDictionary().get("username");
  }

  public void setPassword(String _password) {
    getConnectionDictionary().put("password", _password);
  }

  public String getPassword() {
    return (String) getConnectionDictionary().get("password");
  }

  public void setPlugin(String _plugin) {
    getConnectionDictionary().put("plugin", _plugin);
  }

  public String getPlugin() {
    return (String) getConnectionDictionary().get("plugin");
  }

  public void setDriver(String _driver) {
    getConnectionDictionary().put("driver", _driver);
  }

  public String getDriver() {
    return (String) getConnectionDictionary().get("driver");
  }

  public void setURL(String _url) {
    getConnectionDictionary().put("URL", _url);
  }

  public String getURL() {
    return (String) getConnectionDictionary().get("URL");
  }

  public void setConnectionDictionary(Map _connectionDictionary) {
    setConnectionDictionary(_connectionDictionary, true);
  }

  public void setConnectionDictionary(Map _connectionDictionary, boolean _fireEvents) {
    Map oldConnectionDictionary = myConnectionDictionary;
    myConnectionDictionary = mapChanged(myConnectionDictionary, _connectionDictionary, myConnectionDictionaryRepeater, false);
    if (_fireEvents) {
      firePropertyChange(myConnectionDictionaryRepeater.getPropertyName(), oldConnectionDictionary, myConnectionDictionary);
    }
  }

  public Map getConnectionDictionary() {
    return myConnectionDictionary;
  }

  public void loadFromFolder(File _modelFolder, Set _failures) throws EOModelException, IOException {
    File indexFile = new File(_modelFolder, "index.eomodeld");
    if (!indexFile.exists()) {
      throw new EOModelException(indexFile + " does not exist.");
    }
    myModelFolder = _modelFolder;
    Map rawModelMap = (Map) PropertyListSerialization.propertyListFromFile(indexFile, new EOModelParserDataStructureFactory());
    if (rawModelMap == null) {
      throw new EOModelException(indexFile + " is corrupted.");
    }
    EOModelMap modelMap = new EOModelMap(rawModelMap);
    myModelMap = modelMap;
    Object version = modelMap.get("EOModelVersion");
    if (version instanceof String) {
      myVersion = (String) version;
    }
    else if (version instanceof Number) {
      myVersion = String.valueOf(((Number) version).floatValue());
    }
    else {
      throw new IllegalArgumentException("Unknown version format:" + version);
    }
    myAdaptorName = modelMap.getString("adaptorName", true);
    setConnectionDictionary(modelMap.getMap("connectionDictionary", true), false);
    loadUserInfo(modelMap);

    Set entities = modelMap.getSet("entities");
    if (entities != null) {
      Iterator entitiesIter = entities.iterator();
      while (entitiesIter.hasNext()) {
        EOModelMap entityMap = new EOModelMap((Map) entitiesIter.next());
        String entityName = entityMap.getString("name", true);
        EOEntity entity = new EOEntity();
        File entityFile = new File(_modelFolder, entityName + ".plist");
        if (entityFile.exists()) {
          entity.loadFromFile(entityFile, _failures);
          File fspecFile = new File(_modelFolder, entityName + ".fspec");
          if (fspecFile.exists()) {
            entity.loadFetchSpecsFromFile(fspecFile, _failures);
          }
          addEntity(entity, false, _failures);
        }
        else {
          _failures.add(new EOModelVerificationFailure("The entity file " + entityFile.getAbsolutePath() + " was missing."));
        }
      }
    }

    Set storedProcedureNames = modelMap.getSet("storedProcedures");
    if (storedProcedureNames != null) {
      Iterator storedProcedureNamesIter = storedProcedureNames.iterator();
      while (storedProcedureNamesIter.hasNext()) {
        String storedProcedureName = (String) storedProcedureNamesIter.next();
        EOStoredProcedure storedProcedure = new EOStoredProcedure();
        File storedProcedureFile = new File(_modelFolder, storedProcedureName + ".storedProcedure");
        if (storedProcedureFile.exists()) {
          storedProcedure.loadFromFile(storedProcedureFile, _failures);
          addStoredProcedure(storedProcedure, false, _failures);
        }
        else {
          _failures.add(new EOModelVerificationFailure("The stored procedure file " + storedProcedureFile.getAbsolutePath() + " was missing."));
        }
      }
    }

    Map internalInfoMap = modelMap.getMap("internalInfo");
    if (internalInfoMap != null) {
      Set deletedEntityNamesInObjectStore = modelMap.getSet("_deletedEntityNamesInObjectStore", true);
      if (deletedEntityNamesInObjectStore != null) {
        myDeletedEntityNamesInObjectStore = deletedEntityNamesInObjectStore;
      }
    }

    EOModelMap entityModelerMap = new EOModelMap((Map) getUserInfo().get(EOModel.ENTITY_MODELER_KEY));
    Map databaseConfigs = entityModelerMap.getMap("databaseConfigs");
    if (databaseConfigs != null) {
      Iterator databaseConfigsIter = databaseConfigs.entrySet().iterator();
      while (databaseConfigsIter.hasNext()) {
        Map.Entry databaseConfigEntry = (Map.Entry) databaseConfigsIter.next();
        String name = (String) databaseConfigEntry.getKey();
        EODatabaseConfig databaseConfig = new EODatabaseConfig(name);
        databaseConfig.loadFromMap(new EOModelMap((Map) databaseConfigEntry.getValue()), _failures);
        addDatabaseConfig(databaseConfig, false, _failures);
      }
    }
  }

  public EOModelMap toMap() {
    EOModelMap modelMap = myModelMap.cloneModelMap();
    modelMap.setString("EOModelVersion", myVersion, true);
    modelMap.setString("adaptorName", myAdaptorName, true);
    modelMap.put("connectionDictionary", myConnectionDictionary);

    Set entities = new PropertyListSet();
    Set entitiesWithSharedObjects = new PropertyListSet();
    Iterator entitiesIter = myEntities.iterator();
    while (entitiesIter.hasNext()) {
      EOEntity entity = (EOEntity) entitiesIter.next();
      EOModelMap entityMap = new EOModelMap();
      entityMap.setString("className", entity.getClassName(), true);
      EOEntity parent = entity.getParent();
      String parentName = (parent == null) ? null : parent.getName();
      entityMap.setString("parent", parentName, true);
      entityMap.setString("name", entity.getName(), true);
      entities.add(entityMap);
      if (entity.hasSharedObjects()) {
        entitiesWithSharedObjects.add(entity.getName());
      }
    }
    modelMap.setSet("entities", entities, true);

    modelMap.setSet("entitiesWithSharedObjects", entitiesWithSharedObjects, true);

    Map internalInfoMap = modelMap.getMap("internalInfo");
    if (internalInfoMap == null) {
      internalInfoMap = new HashMap();
    }
    if (myDeletedEntityNamesInObjectStore != null && !myDeletedEntityNamesInObjectStore.isEmpty()) {
      internalInfoMap.put("_deletedEntityNamesInObjectStore", myDeletedEntityNamesInObjectStore);
    }
    else {
      internalInfoMap.remove("_deletedEntityNamesInObjectStore");
    }
    modelMap.setMap("internalInfo", internalInfoMap, true);

    Set storedProcedures = new PropertyListSet();
    Iterator storedProceduresIter = myStoredProcedures.iterator();
    while (storedProceduresIter.hasNext()) {
      EOStoredProcedure storedProcedure = (EOStoredProcedure) storedProceduresIter.next();
      storedProcedures.add(storedProcedure.getName());
    }
    modelMap.setSet("storedProcedures", storedProcedures, true);

    EOModelMap entityModelerMap = new EOModelMap((Map) getUserInfo().get(EOModel.ENTITY_MODELER_KEY));
    Map databaseConfigs = new PropertyListMap();
    Iterator databaseConfigsIter = myDatabaseConfigs.iterator();
    while (databaseConfigsIter.hasNext()) {
      EODatabaseConfig databaseConfig = (EODatabaseConfig) databaseConfigsIter.next();
      databaseConfigs.put(databaseConfig.getName(), databaseConfig.toMap());
    }
    entityModelerMap.setMap("databaseConfigs", databaseConfigs, true);
    if (entityModelerMap.isEmpty()) {
      getUserInfo().remove(EOModel.ENTITY_MODELER_KEY);
    }
    else {
      getUserInfo().put(EOModel.ENTITY_MODELER_KEY, entityModelerMap);
    }

    writeUserInfo(modelMap);

    return modelMap;
  }

  public File saveToFolder(File _parentFolder) throws IOException {
    File modelFolder;
    if (_parentFolder.getName().endsWith(".eomodeld")) {
      modelFolder = _parentFolder;
    }
    else {
      modelFolder = new File(_parentFolder, myName + ".eomodeld");
    }
    if (!modelFolder.exists()) {
      if (!modelFolder.mkdirs()) {
        throw new IOException("Failed to create folder '" + modelFolder + "'.");
      }
    }
    myModelFolder = modelFolder;
    File indexFile = new File(modelFolder, "index.eomodeld");
    EOModelMap modelMap = toMap();
    PropertyListSerialization.propertyListToFile(indexFile, modelMap);

    if (myDeletedEntityNames != null) {
      Iterator deletedEntityNameIter = myDeletedEntityNames.iterator();
      while (deletedEntityNameIter.hasNext()) {
        String entityName = (String) deletedEntityNameIter.next();
        File entityFile = new File(modelFolder, entityName + ".plist");
        if (entityFile.exists()) {
          entityFile.delete();
        }
        File fspecFile = new File(modelFolder, entityName + ".fspec");
        if (fspecFile.exists()) {
          fspecFile.delete();
        }
      }
    }

    Iterator entitiesIter = myEntities.iterator();
    while (entitiesIter.hasNext()) {
      EOEntity entity = (EOEntity) entitiesIter.next();
      String entityName = entity.getName();
      File entityFile = new File(modelFolder, entityName + ".plist");
      entity.saveToFile(entityFile);
      File fspecFile = new File(modelFolder, entityName + ".fspec");
      entity.saveFetchSpecsToFile(fspecFile);
    }

    if (myDeletedStoredProcedureNames != null) {
      Iterator deletedStoredProcedureNameIter = myDeletedStoredProcedureNames.iterator();
      while (deletedStoredProcedureNameIter.hasNext()) {
        String storedProcedureName = (String) deletedStoredProcedureNameIter.next();
        File storedProcedureFile = new File(modelFolder, storedProcedureName + ".storedProcedure");
        if (storedProcedureFile.exists()) {
          storedProcedureFile.delete();
        }
      }
    }

    Iterator storedProceduresIter = myStoredProcedures.iterator();
    while (storedProceduresIter.hasNext()) {
      EOStoredProcedure storedProcedure = (EOStoredProcedure) storedProceduresIter.next();
      String storedProcedureName = storedProcedure.getName();
      File storedProcedureFile = new File(modelFolder, storedProcedureName + ".storedProcedure");
      storedProcedure.saveToFile(storedProcedureFile);
    }

    return modelFolder;
  }

  public void resolve(Set _failures) {
    Iterator entitiesIter = myEntities.iterator();
    while (entitiesIter.hasNext()) {
      EOEntity entity = (EOEntity) entitiesIter.next();
      entity.resolve(_failures);
    }
  }

  public void verify(Set _failures) {
    // TODO

    Iterator entitiesIter = myEntities.iterator();
    while (entitiesIter.hasNext()) {
      EOEntity entity = (EOEntity) entitiesIter.next();
      entity.verify(_failures);
    }
  }

  public String getFullyQualifiedName() {
    return myName;
  }

  public String toString() {
    return "[EOModel: name = " + myName + "; entities = " + myEntities + "]";
  }

  /** Begin Prototypes **/
  public synchronized void clearCachedPrototypes(Set _failures, boolean _reload) {
    myPrototypeAttributeCache = null;
    Iterator entitiesIter = myEntities.iterator();
    while (entitiesIter.hasNext()) {
      EOEntity entity = (EOEntity) entitiesIter.next();
      entity.clearCachedPrototypes(_failures, _reload);
    }
  }

  public synchronized Set getPrototypeAttributeNames() {
    Set prototypeAttributeNames = new TreeSet();
    Iterator prototypeAttributesIter = getPrototypeAttributes().iterator();
    while (prototypeAttributesIter.hasNext()) {
      EOAttribute attribute = (EOAttribute) prototypeAttributesIter.next();
      prototypeAttributeNames.add(attribute.getName());
    }
    return prototypeAttributeNames;
  }

  public String getDefaultPrototypeEntityName() {
    return "EOPrototypes";
  }

  public String getAdaptorPrototypeEntityName() {
    String adaptorName = getAdaptorName();
    String adaptorPrototypeEntityName = null;
    if (adaptorName != null) {
      adaptorPrototypeEntityName = "EO" + adaptorName + "Prototypes";
    }
    return adaptorPrototypeEntityName;
  }

  public String getDriverPrototypeEntityName() {
    String driverPrototypeEntityName = null;
    String adaptorName = getAdaptorName();
    // MS: Hardcoded JDBC reference hack ...
    if ("JDBC".equals(adaptorName)) {
      Map connectionDictionary = getConnectionDictionary();
      if (connectionDictionary != null) {
        String jdbcUrl = (String) connectionDictionary.get("URL");
        if (jdbcUrl != null) {
          int firstColon = jdbcUrl.indexOf(':');
          int secondColon = jdbcUrl.indexOf(':', firstColon + 1);
          if (firstColon != -1 && secondColon != -1) {
            String driverName = jdbcUrl.substring(firstColon + 1, secondColon);
            driverPrototypeEntityName = "EOJDBC" + driverName + "Prototypes";
          }
        }
      }
    }
    return driverPrototypeEntityName;
  }

  public EOEntity getPreferredPrototypeEntity() {
    EOEntity prototypeEntity = null;
    String driverPrototypeEntityName = getDriverPrototypeEntityName();
    if (driverPrototypeEntityName != null) {
      prototypeEntity = myModelGroup.getEntityNamed(driverPrototypeEntityName);
    }
    if (prototypeEntity == null) {
      String adaptorPrototypeEntityName = getAdaptorPrototypeEntityName();
      if (adaptorPrototypeEntityName != null) {
        prototypeEntity = myModelGroup.getEntityNamed(adaptorPrototypeEntityName);
      }
    }
    if (prototypeEntity == null) {
      String defaultPrototypeEntityName = getDefaultPrototypeEntityName();
      prototypeEntity = myModelGroup.getEntityNamed(defaultPrototypeEntityName);
    }
    return prototypeEntity;
  }

  public synchronized Set getPrototypeAttributes() {
    if (myPrototypeAttributeCache == null) {
      Map prototypeAttributeCache = new HashMap();

      Set prototypeEntityNames = new HashSet();
      addPrototypeAttributes(getDefaultPrototypeEntityName(), prototypeEntityNames, prototypeAttributeCache);

      String adaptorPrototypeEntityName = getAdaptorPrototypeEntityName();
      addPrototypeAttributes(adaptorPrototypeEntityName, prototypeEntityNames, prototypeAttributeCache);

      String driverPrototypeEntityName = getDriverPrototypeEntityName();
      addPrototypeAttributes(driverPrototypeEntityName, prototypeEntityNames, prototypeAttributeCache);

      // Do we need to support "EOPrototypesToHide" entity?
      myPrototypeAttributeCache = new HashSet();
      myPrototypeAttributeCache.addAll(prototypeAttributeCache.values());
    }
    return myPrototypeAttributeCache;
  }

  protected void addPrototypeAttributes(String _prototypeEntityName, Set _prototypeEntityNames, Map _prototypeAttributeCache) {
    if (_prototypeEntityName != null && !_prototypeEntityNames.contains(_prototypeEntityName)) {
      _prototypeEntityNames.add(_prototypeEntityName);
      EOEntity prototypeEntity = myModelGroup.getEntityNamed(_prototypeEntityName);
      if (prototypeEntity != null) {
        Iterator attributeIter = prototypeEntity.getAttributes().iterator();
        while (attributeIter.hasNext()) {
          EOAttribute prototypeAttribute = (EOAttribute) attributeIter.next();
          _prototypeAttributeCache.put(prototypeAttribute.getName(), prototypeAttribute);
        }
      }
    }
  }

  public EOAttribute getPrototypeAttributeNamed(String _name) {
    EOAttribute matchingAttribute = null;
    Set prototypeAttributes = getPrototypeAttributes();
    Iterator attributesIter = prototypeAttributes.iterator();
    while (matchingAttribute == null && attributesIter.hasNext()) {
      EOAttribute attribute = (EOAttribute) attributesIter.next();
      if (attribute.getName().equals(_name)) {
        matchingAttribute = attribute;
      }
    }
    return matchingAttribute;
  }

  /** End Prototypes **/

  public static void main(String[] args) throws IOException, EOModelException {
    Set failures = new LinkedHashSet();

    EOModelGroup modelGroup = new EOModelGroup();
    modelGroup.addModelsFromFolder(new File("/Library/Frameworks/ERPrototypes.framework/Resources"), false, failures);
    modelGroup.addModelsFromFolder(new File("/Users/mschrag/Documents/workspace/MDTask"), false, failures);
    modelGroup.addModelsFromFolder(new File("/Users/mschrag/Documents/workspace/MDTAccounting"), false, failures);
    modelGroup.addModelsFromFolder(new File("/Users/mschrag/Documents/workspace/MDTCMS"), false, failures);
    modelGroup.addModelsFromFolder(new File("/Users/mschrag/Documents/workspace/MDTWOExtensions"), false, failures);

    modelGroup.resolve(failures);
    modelGroup.verify(failures);
    Iterator failuresIter = failures.iterator();
    while (failuresIter.hasNext()) {
      EOModelVerificationFailure failure = (EOModelVerificationFailure) failuresIter.next();
      System.out.println("EOModel.main: " + failure);
    }

    File outputPath = new File("/tmp");
    System.out.println("EOModel.main: Saving model to " + outputPath + " ...");
    EOModel mdtaskModel = modelGroup.getModelNamed("MDTask");
    mdtaskModel.saveToFolder(outputPath);
    System.out.println("EOModel.main: Done.");
  }
}