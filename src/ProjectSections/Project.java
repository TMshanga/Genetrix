package projectSections;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.io.ByteStreams;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import dataStructures.Tree;
import dataStructures.Trie;
import dataStructures.Tree.Node;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.web.WebView;
import junit.framework.Test;
import main.Main;
import main.ContentsPage.MyTreeItem;

public class Project{	
	public BiMap<String,Image> imageMap = HashBiMap.create();
	public BiMap<String,Node<Page>> pageMap = HashBiMap.create();
	public Tree<Page> pageTree = new Tree<>(new Book());
	
	public Project() {
		pageTree = new Tree<Page>(new Book());
	}
	public Project(String title) {
		pageTree = new Tree<Page>(new Book(title));
	}
	
	public byte[] encode() {
		byte[] treeData = encodeTree();
		byte[] mapData = encodePageMap();
		byte[] imageData = encodeImageMap();
		
		treeData = Bytes.concat(Ints.toByteArray(treeData.length),treeData);
		mapData = Bytes.concat(Ints.toByteArray(mapData.length),mapData);		
		byte[] data = Bytes.concat(treeData,mapData);
		data = Bytes.concat(data,imageData);
		return Bytes.concat(Ints.toByteArray(data.length),data);
	}
	public void decode(byte[] data) {
		reset();
		int treeDataLen = ByteBuffer.wrap(data, 4,4).getInt();
		int mapDataLen = ByteBuffer.wrap(data, treeDataLen+8,4).getInt();
		decodeTree(data,8,treeDataLen);
		decodePageMap(data,12+treeDataLen,mapDataLen);
		decodeImageMap(data,12+treeDataLen+mapDataLen,data.length - (12+treeDataLen+mapDataLen));
	}
	
	private void reset() {
		pageMap = HashBiMap.create();
		pageTree = new Tree<>(new Book());
	}
	private byte[] encodeImageMap() {
		byte[] allBytes = new byte[0];
		for(String key: imageMap.keySet()) {
			BufferedImage image = SwingFXUtils.fromFXImage(imageMap.get(key), null);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {ImageIO.write(image, "png", baos);	} catch (IOException e) {e.printStackTrace();}
			byte[] imageBytes = baos.toByteArray();
			byte[] keyBytes = key.getBytes();
			keyBytes = Bytes.concat(Ints.toByteArray(keyBytes.length),keyBytes);
			imageBytes = Bytes.concat(Ints.toByteArray(imageBytes.length),imageBytes);
			byte[] data = Bytes.concat(keyBytes,imageBytes);
			allBytes = Bytes.concat(allBytes,data);
		}
		return allBytes;
	}
	private void decodeImageMap(byte[] data, int offset, int length) {
		imageMap = HashBiMap.create();	
		int index = offset;
		while (index<offset+length) {
			int imageKeyLen = ByteBuffer.wrap(data, index, 4).getInt();
			String imageKey = new String(data, index+4, imageKeyLen);
			int imageLen = ByteBuffer.wrap(data, index+imageKeyLen+4, 4).getInt();
			Image image = new Image(new ByteArrayInputStream(data,index+imageKeyLen+8,imageLen));
			imageMap.put(imageKey, image);
			index+= imageKeyLen+imageLen+8;
		}
	}
	
	private byte[] encodePageMap() {
		byte[] allBytes = new byte[0];
		for(String key: pageMap.keySet()){			
			byte[] addressData = new byte[0];
			for(Integer index: pageMap.get(key).getAddress()) {
				addressData = Bytes.concat(addressData,Ints.toByteArray(index));
			}
			byte[] keyBytes = key.getBytes();
			keyBytes = Bytes.concat(Ints.toByteArray(keyBytes.length),keyBytes);
			byte[] addressBytes = Bytes.concat(Ints.toByteArray(addressData.length),addressData);
			byte[] data = Bytes.concat(keyBytes,addressBytes);
			allBytes = Bytes.concat(allBytes,data);
		}
		return allBytes;
	}
	
	private void decodePageMap(byte[] data, int offset, int length) {
		pageMap = HashBiMap.create();
		
		int index = offset;
		while (index<offset + length) {
			int keyLen = ByteBuffer.wrap(data, index, 4).getInt();
			String key = new String(data, index+4,keyLen);	
			int addressLen = ByteBuffer.wrap(data, index+keyLen+4, 4).getInt();

			ArrayList<Integer> address = new ArrayList<Integer>();			
			
			for(int i=index+keyLen+8;i<index+keyLen+addressLen+8;i+=4) {
				address.add(ByteBuffer.wrap(data,i,4).getInt());
			}
			pageMap.put(key, Main.currentProject.pageTree.getNode(address));
			index += keyLen+addressLen+8;
		}
	}
	
	private byte[] encodeTree() {
		byte[] treeData = new byte[0];
		
		ArrayList<WrappedNode> nodeList = new ArrayList<WrappedNode>();		
		wrapAllNodes(pageTree.root,null,null,nodeList);	
		
		for(WrappedNode preppedNode: nodeList){
			byte[] encodedNode = preppedNode.encode();
			byte[] data = Bytes.concat(Ints.toByteArray(encodedNode.length),encodedNode);
			treeData = Bytes.concat(treeData,data);
		}	
		return treeData;
	}
	
	private void decodeTree(byte[] data, int offset, int length) {
		int index = offset;
		while (index<offset+length) {
			WrappedNode cont = new WrappedNode();
			int containerLen = ByteBuffer.wrap(data,index,4).getInt();
			cont.decode(data,index+4,containerLen);	
			Main.currentProject.pageTree.setNode(cont.address, cont.pageNode);		
			index += containerLen+4;
		}
	}
	
	private void wrapAllNodes(Tree.Node<Page> pageNode, WrappedNode containedParent, ArrayList<Tree.Node<Page>> visited, ArrayList<WrappedNode> wrappedNodeList ) {
		WrappedNode nodeStruct;
		if (containedParent !=null) 
			nodeStruct = new WrappedNode(pageNode,containedParent.address, containedParent.pageNode.getChildren().indexOf(pageNode));
		else
			nodeStruct = new WrappedNode(pageNode, new ArrayList<Integer>(), 0);
		wrappedNodeList.add(nodeStruct);
		if (visited == null)
			visited = new ArrayList<Tree.Node<Page>>();
		visited.add(pageNode);
		for (int i = 0; i < pageNode.getChildren().size(); i++) {
			Tree.Node<Page> childPage = pageNode.getChildren().get(i);
			if (childPage != null && !visited.contains(childPage)) {
				wrapAllNodes(childPage, nodeStruct, visited, wrappedNodeList);
			}
		}
	}
	
	class WrappedNode{
		Tree.Node<Page> pageNode;
		ArrayList<Integer> address = new ArrayList<Integer>(); 
		
		WrappedNode(){}
		WrappedNode(Tree.Node<Page> page, ArrayList<Integer> parentAddress, int addressIndex){
			this.pageNode = page;
			address.addAll(parentAddress);
			address.add(addressIndex);
		}
		public byte[] encode() {
			byte[] addressData = new byte[0];
			for(Integer index: address) {
				addressData = Bytes.concat(addressData,Ints.toByteArray(index));
			}
			addressData = Bytes.concat(Ints.toByteArray(addressData.length),addressData);
			byte[] data = Bytes.concat(addressData,pageNode.data.encode());	
			return data;
		}
		
		public void decode(byte[] data, int offset, int length) {
			
			ArrayList<Integer> address = new ArrayList<Integer>();
			
			int addressLen = ByteBuffer.wrap(data,offset,4).getInt();
			
			for(int i=offset+4;i<offset+4+addressLen;i+=4) {
				address.add(ByteBuffer.wrap(data,i,4).getInt());
			}
			this.address = address;
			
			Page page = null;		
			int pageType = ByteBuffer.wrap(data, offset+addressLen+4, 4).getInt();
			if(pageType == Page.pageTypes.BasicPage.toInt()) 	page = new BasicPage();
			if(pageType == Page.pageTypes.Folder.toInt())  page = new Folder();
			if(pageType == Page.pageTypes.Book.toInt()) 	page = new Book();
			page.decode(data, offset+addressLen+4, length-(addressLen+4));
			
			this.pageNode = new Node<Page>(page);		
		}
	}
	
	public void readyImage(String key) {
		String jarDir = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		jarDir = (new File(jarDir)).getParentFile().getPath();
		try {jarDir = URLDecoder.decode(jarDir, "UTF-8");} catch (UnsupportedEncodingException e) {e.printStackTrace();}
		File path = new File(jarDir + "/data"); 
		if(!path.exists()) path.mkdir();
		path = new File(path.toString() + "/IMG" + key + ".png");
		if(!path.exists()) {
		    BufferedImage image = SwingFXUtils.fromFXImage(imageMap.get(key), null);
		    try {ImageIO.write(image, "png", path);} catch (IOException e) {e.printStackTrace();}
		}
	}
	
	public void readyAllImages() {
		for(String key: imageMap.keySet())
			readyImage(key);
	}
	
	public void clearImageDir() {
		String jarDir = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		jarDir = (new File(jarDir)).getParentFile().getPath();
		try {jarDir = URLDecoder.decode(jarDir, "UTF-8");} catch (UnsupportedEncodingException e) {e.printStackTrace();}
		File path = new File(jarDir + "/data"); 
		if(path.exists())
			try {FileUtils.cleanDirectory(path);} catch (IOException e) {e.printStackTrace();}
	}
	
	public void clearUnusedImages()
 	{
		HashSet<String> unusedImageKeys = new HashSet<>();
		unusedImageKeys.addAll(imageMap.keySet());
 		clearUnusedImages(Main.currentProject.pageTree.root,new ArrayList<Node<Page>>(), unusedImageKeys);
 		for(String key: unusedImageKeys) {
 			imageMap.remove(key);
 		}
 	}
	
	private void clearUnusedImages(Node<Page> node, ArrayList<Node<Page>> visited, Set<String> unusedImageKeys)
 	{
 		if(visited ==null)visited = new ArrayList<Node<Page>>();
        visited.add(node);
        if(node.data instanceof BasicPage) {
        	String pageContent = ((BasicPage)node.data).content;
        	for(String key: imageMap.keySet()) {
        		if(pageContent.contains("data/IMG" + key + ".png"))
        			unusedImageKeys.remove(key);
        	}
        }
 		for (int i = 0; i < node.getChildren().size(); i++) {
 			Node<Page> n = node.getChildren().get(i);
 			if(n!=null && !visited.contains(n))
 			{
 				clearUnusedImages(n,visited,unusedImageKeys);
 			}
 		}
 	}
	
	public String addImage(Image image){
		byte[] bytes;
		String key = UUID.randomUUID().toString();
		imageMap.put(key, image);
		readyImage(key);
		String jarDir = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		jarDir = (new File(jarDir)).getParentFile().getPath();
		try {jarDir = URLDecoder.decode(jarDir, "UTF-8").replace("\\", "/");} catch (UnsupportedEncodingException e) {e.printStackTrace();}
		System.out.println(jarDir);
		return jarDir + "/data/IMG" + key + ".png";	    
	}	
}
