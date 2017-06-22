package auxiliary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class LocalityTranslation 
{	
	BiMap<Integer, String> nodeAndIPAddressTable = null;
	private LocalityTranslation() {
		nodeAndIPAddressTable = loadIPTable();
	}
	
	// Inner class initializes instance on load, won't be loaded
	// until referenced by getInstance()
	private static class LocalityTranslationHolder {
		public static final LocalityTranslation instance = new LocalityTranslation();
	}
	// Return the singleton instance.
	public static LocalityTranslation getInstance() { return LocalityTranslationHolder.instance; }
	
	
	private BiMap<Integer, String> loadIPTable()
	{
		String fileName = "data\\local_address_port.txt";
		// all edges
		BiMap<Integer, String> nodeAndIPAddressTable = HashBiMap.create(); 
		
		File weightsFile = new File(fileName);
		if(weightsFile.exists() && !weightsFile.isDirectory()) { 
			
			FileReader fileReader = null;
			try {
				File file = new File(fileName);
				fileReader = new FileReader(file);
				BufferedReader bufferedReader = new BufferedReader(fileReader);

				String line;
				while ((line = bufferedReader.readLine()) != null) {
					line = line.replaceAll("\t", " ");
					String[] parts = line.split(" ");
					int nodeID = Integer.valueOf(parts[0]);
					String ipAddress = parts[1];			
					nodeAndIPAddressTable.put(nodeID, ipAddress);
				}
				return nodeAndIPAddressTable;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			} finally{
				if (fileReader != null) {
					try {
						fileReader.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		} else
			return null;
	}
	
	public Integer getNodeID(String ip)
	{
		Integer nodeID = nodeAndIPAddressTable.inverse().get(ip);
		return nodeID;
	}
	
	
	public String getIP(Integer nodeID)
	{
		String ip = nodeAndIPAddressTable.get(nodeID);
		return ip;
	}
	
	
}
