package com.terraforged.mod.data.export;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.file.PathUtils;

public class Main {
	private static final String MCMETA_PATH = "pack.mcmeta";
	private static final String LOGO_PATH = "logo.png";

	public static void main(String[] args) throws IOException {
		if (args.length < 1) {
			throw new IllegalStateException("Missing required arguments");
		}

		Path datapackZip = args.length > 0 ? Path.of(args[0]) : Path.of(".").resolve("terraforged.zip");
		
		Map<String, String> env = new HashMap<>();
		env.put("create", "true");
		URI uri = URI.create("jar:" + datapackZip.toUri());
		try (FileSystem fileSystem = FileSystems.newFileSystem(uri, env)) {
			System.out.println(String.format("Copying default pack to zip %s", datapackZip));
			copyTFDatapack(fileSystem);
		}
	}

	private static void copyTFDatapack(FileSystem fileSystem) throws IOException {
		//copy json files
		for(String strPath : enumerate((path) -> path.endsWith(".json"), "data")) {
			Path path = fileSystem.getPath(strPath);
			PathUtils.createParentDirectories(path);
			try(InputStream stream = Main.class.getResourceAsStream("/" + strPath)) {
				Files.copy(stream, path, StandardCopyOption.REPLACE_EXISTING);
			}
		}
			
		// copy mcmeta
		try(InputStream stream = Main.class.getResourceAsStream("/" + MCMETA_PATH)) {
			Files.copy(stream, fileSystem.getPath(MCMETA_PATH), StandardCopyOption.REPLACE_EXISTING);
		}
			
		// copy logo
		try(InputStream stream = Main.class.getResourceAsStream("/" + LOGO_PATH)) {
			Files.copy(stream, fileSystem.getPath(LOGO_PATH), StandardCopyOption.REPLACE_EXISTING);
		}
	}
	
	private static List<String> enumerate(Predicate<String> predicate, String root) throws IOException {
		List<String> paths = new ArrayList<>();
		if (predicate.test(root)) {
			paths.add(root);
		} else {
			try (InputStream in = Main.class.getClassLoader().getResourceAsStream(root)) {
				for (String path : IOUtils.readLines(in, StandardCharsets.UTF_8)) {
					paths.addAll(enumerate(predicate, root + "/" + path));
				}
			}
		}
		return paths;
	}
}
