package tp.fil.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.gmt.modisco.java.BodyDeclaration;
import org.eclipse.gmt.modisco.java.CompilationUnit;
import org.eclipse.gmt.modisco.java.FieldDeclaration;
import org.eclipse.gmt.modisco.java.Model;
import org.eclipse.gmt.modisco.java.emf.JavaPackage;

public class DataComputation {
	
	static EPackage dataPackage;
	
	public static void main(String[] args) {
		try {
			Resource javaModel;
			Resource dataModel;
			Resource dataMetamodel;
			
			ResourceSet resSet = new ResourceSetImpl();
			resSet.getResourceFactoryRegistry().
				getExtensionToFactoryMap().
				put("ecore", new EcoreResourceFactoryImpl());
			resSet.getResourceFactoryRegistry().
				getExtensionToFactoryMap().
				put("xmi", new XMIResourceFactoryImpl());
			
			JavaPackage.eINSTANCE.eClass();
			
			dataMetamodel = resSet.createResource(URI.createFileURI("src/tp/fil/resources/Data.ecore"));
			dataMetamodel.load(null);
			EPackage.Registry.INSTANCE.put("http://data", 
					dataMetamodel.getContents().get(0));
			
			javaModel = resSet.createResource(URI.createFileURI("../PetStore/PetStore_java.xmi"));
			javaModel.load(null);
			
			dataModel = resSet.createResource(URI.createFileURI("../PetStore/PetStore_data_java.xmi"));
			
			/*
			 * Beginning of the part to be completed...
			 */
			
			dataPackage = (EPackage) dataMetamodel.getContents().get(0);
			
			TreeIterator<EObject> iterator;
			iterator = javaModel.getAllContents();
			if(iterator.hasNext()) {
				Model sourceModel = (Model) iterator.next();
				EObject targetModel = getModele(sourceModel);
				dataModel.getContents().add(targetModel);	
			}
			
			/*
			 * End of the part to be completed...
			 */
			
			dataModel.save(null);
			
			javaModel.unload();
			dataModel.unload();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static EObject getModele(Model model)
	{
		EClass modelClass = (EClass) dataPackage.getEClassifier("Model");
		EObject targetModel = dataPackage.getEFactoryInstance().create(modelClass);
		EStructuralFeature nameModel = targetModel.eClass().getEStructuralFeature("name");
		EStructuralFeature listClasses = targetModel.eClass().getEStructuralFeature("classes");
		targetModel.eSet(nameModel, model.getName());
		targetModel.eSet(listClasses, getClasses(model));
		return targetModel;
	}
	

	public static List<EObject> getClasses(Model model)
	{
		List<EObject> listClasses = new ArrayList<>();
		EClass classClass = (EClass) dataPackage.getEClassifier("Class");
		for(CompilationUnit compilationUnit : model.getCompilationUnits())
		{
			if(compilationUnit.getPackage().getName().startsWith("model")) {
				System.out.println(compilationUnit);
				EObject targetClasse = dataPackage.getEFactoryInstance().create(classClass);
				EStructuralFeature nameClass = classClass.getEStructuralFeature("name");
				EStructuralFeature listAttributes = classClass.getEStructuralFeature("attributes");
				targetClasse.eSet(nameClass, compilationUnit.getName());
				targetClasse.eSet(listAttributes, getAttributes(compilationUnit));		
				listClasses.add(targetClasse);
			}
		}
		return listClasses;
	}
	
	public static List<EObject> getAttributes(CompilationUnit compilationUnit)
	{
		List<EObject> listAttributes = new ArrayList<>();
		EClass classAttribute = (EClass) dataPackage.getEClassifier("Attribute");
		for(BodyDeclaration bodyDeclaration : compilationUnit.getTypes().get(0).getBodyDeclarations())
		{
			if(bodyDeclaration instanceof FieldDeclaration && bodyDeclaration.getOriginalCompilationUnit() != null && bodyDeclaration.getOriginalCompilationUnit().getPackage().getName().startsWith("model"))
			{
				System.out.println("\t" + ((FieldDeclaration) bodyDeclaration).getFragments().get(0));
				EObject targetAttribute = dataPackage.getEFactoryInstance().create(classAttribute);
				EStructuralFeature nameAttribute = classAttribute.getEStructuralFeature("name");
				EStructuralFeature typeAttribute = classAttribute.getEStructuralFeature("type");
				targetAttribute.eSet(nameAttribute, ((FieldDeclaration) bodyDeclaration).getFragments().get(0).getName());
				targetAttribute.eSet(typeAttribute, ((FieldDeclaration) bodyDeclaration).getType().getType().getName());
				listAttributes.add(targetAttribute);
			}
		}
		
		return listAttributes;
	}
}
