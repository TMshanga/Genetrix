package projectSections;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import main.ContentsPage;
import main.Main;

public class Project{	
	public BiMap<String,Image> imageMap = HashBiMap.create();
	public BiMap<String,TreeItem<Page>> pageMap = HashBiMap.create();
	
	public byte[] encode() {
		byte[] treeData = encodeTree();
		byte[] imageData = encodeImageMap();
		
		treeData = Bytes.concat(Ints.toByteArray(treeData.length),treeData);
		byte[] data = Bytes.concat(treeData,imageData);
		return Bytes.concat(Ints.toByteArray(data.length),data);
	}
	public void decode(byte[] data) {
		pageMap.clear();
		Main.contentsPage.tree.setRoot(new TreeItem<Page>());
		
		int treeDataLen = ByteBuffer.wrap(data, 4,4).getInt();
		decodeTree(data,8,treeDataLen);
		decodeImageMap(data,8+treeDataLen,data.length - (8+treeDataLen));
	}
	
	private byte[] encodeImageMap() {
		byte[] allBytes = new byte[0];
		for(String key: imageMap.keySet()) {
			BufferedImage image = SwingFXUtils.fromFXImage(imageMap.get(key), null);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {ImageIO.write(image, "png", baos);	} catch (IOException e) {e.printStackTrace();return null;}
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
	
	private byte[] encodeTree() {
		byte[] treeData = new byte[0];
		
		ArrayList<WrappedNode> nodeList = new ArrayList<WrappedNode>();		
		wrapAllNodes(Main.contentsPage.tree.getRoot(),null,null,nodeList);	
		
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
			WrappedNode wrappedNode = new WrappedNode();
			int nodeLen = ByteBuffer.wrap(data,index,4).getInt();
			wrappedNode.decode(data,index+4,nodeLen);	
			ContentsPage.setNode(wrappedNode.address,Main.contentsPage.tree,wrappedNode.pageNode);
			Main.currentProject.pageMap.put(wrappedNode.pageMapKey, wrappedNode.pageNode);
			index += nodeLen+4;
		}
	}
	
	private void wrapAllNodes(TreeItem<Page> pageNode, WrappedNode containedParent, ArrayList<TreeItem<Page>> visited, ArrayList<WrappedNode> wrappedNodeList ) {
		WrappedNode wrappedNode;
		if (containedParent !=null) 
			wrappedNode = new WrappedNode(pageNode,containedParent.address, containedParent.pageNode.getChildren().indexOf(pageNode));
		else
			wrappedNode = new WrappedNode(pageNode, new ArrayList<Integer>(), 0);
		wrappedNodeList.add(wrappedNode);
		if (visited == null)
			visited = new ArrayList<TreeItem<Page>>();
		visited.add(pageNode);
		for (int i = 0; i < pageNode.getChildren().size(); i++) {
			TreeItem<Page> childPage = pageNode.getChildren().get(i);
			if (childPage != null && !visited.contains(childPage)) {
				wrapAllNodes(childPage, wrappedNode, visited, wrappedNodeList);
			}
		}
	}
	
	class WrappedNode{
		TreeItem<Page> pageNode;
		ArrayList<Integer> address = new ArrayList<Integer>(); 
		String pageMapKey;
		
		WrappedNode(){}
		WrappedNode(TreeItem<Page> pageNode, ArrayList<Integer> parentAddress, int addressIndex, String pageMapKey){
			this.pageNode = pageNode;
			this.pageMapKey = pageMapKey;
			address.addAll(parentAddress);
			address.add(addressIndex);
		}
		WrappedNode(TreeItem<Page> pageNode, ArrayList<Integer> parentAddress, int addressIndex){
			this(pageNode,parentAddress,addressIndex,"");
		}
		public byte[] encode() {
			byte[] addressData = new byte[0];
			for(Integer index: address) {
				addressData = Bytes.concat(addressData,Ints.toByteArray(index));
			}
			addressData = Bytes.concat(Ints.toByteArray(addressData.length),addressData);
			byte[] keyData = pageMapKey.getBytes();
			keyData = Bytes.concat(Ints.toByteArray(keyData.length),keyData);
			byte[] data = Bytes.concat(addressData,pageNode.getValue().encode());
			return Bytes.concat(keyData,data);
		}
		
		public void decode(byte[] data, int offset, int length) {
			
			int keyDataLen = ByteBuffer.wrap(data,offset,4).getInt();
			this.pageMapKey = new String(data,offset+4,keyDataLen);

			this.address = new ArrayList<Integer>();			
			int addressLen = ByteBuffer.wrap(data,offset+keyDataLen+4,4).getInt();
			
			for(int i=offset+keyDataLen+8;i<offset+keyDataLen+8+addressLen;i+=4) {
				address.add(ByteBuffer.wrap(data,i,4).getInt());
			}
			
			Page page = null;		
			int pageType = ByteBuffer.wrap(data, offset+keyDataLen+addressLen+8, 4).getInt();
			if(pageType == Page.pageTypes.BasicPage.toInt()) 	page = new BasicPage();
			if(pageType == Page.pageTypes.Folder.toInt())  page = new Folder();
			if(pageType == Page.pageTypes.Book.toInt()) 	page = new Book();
			page.decode(data, offset+keyDataLen+addressLen+8, length-(addressLen+keyDataLen+8));
			
			this.pageNode = new TreeItem<Page>(page);
		}
	}
	
	public void readyImage(String key) {
		File path = new File(FilenameUtils.concat(Main.getJarDir(), "data").replace("\\", "/")); 
		if(!path.exists()) path.mkdir();
		path = new File(FilenameUtils.concat(path.toString(), "IMG" + key + ".png").replace("\\", "/"));
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
		File path = new File(FilenameUtils.concat(Main.getJarDir(), "data").replace("\\", "/")); 
		if(path.exists())
			try {FileUtils.cleanDirectory(path);} catch (IOException e) {e.printStackTrace();}
	}
	
	public void clearUnusedImages()
 	{
		HashSet<String> unusedImageKeys = new HashSet<>();
		unusedImageKeys.addAll(imageMap.keySet());
 		clearUnusedImages(Main.contentsPage.tree.getRoot(), unusedImageKeys);
 		for(String key: unusedImageKeys) {
 			imageMap.remove(key);
 		}
 	}
	
	private void clearUnusedImages(TreeItem<Page> node, Set<String> unusedImageKeys)
 	{
        if(node.getValue() instanceof BasicPage) {
        	String pageContent = ((BasicPage)node.getValue()).htmlEditor.getHtmlText();
        	for(String key: imageMap.keySet()) {
        		if(pageContent.contains("data/IMG" + key + ".png"))
        			unusedImageKeys.remove(key);
        	}
        }
 		for (int i = 0; i < node.getChildren().size(); i++) {
 			TreeItem<Page> n = node.getChildren().get(i);
 			clearUnusedImages(n,unusedImageKeys);
 		}
 	}
	
	public String addImage(Image image){
		String key = UUID.randomUUID().toString();
		imageMap.put(key, image);
		readyImage(key);
		return FilenameUtils.concat(Main.getJarDir(), "data/IMG" + key + ".png").replace("\\", "/");	
	}	
}
