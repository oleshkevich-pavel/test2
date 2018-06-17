package com.instinctools.test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.instinctools.test.storage.IStorageService;
import com.instinctools.test.storage.StorageException;
import com.instinctools.test.storage.StorageProperties;

@Controller
@RequestMapping(value = "/")
public class FileUploadController {

    private static final Logger LOGGER = LoggerFactory.getLogger("controllerLogger");
	private static final String EXCLUSIONS_PATH = "src/main/resources/exclusion.txt";
	private static final String SPLIT_SYMBOLS = "[^a-zA-Zа-яА-Я]";
	private static final String BRACKETS = ")}]({[";
	private static List<String> EXCLUSION = new ArrayList<String>();

	private final IStorageService storageService;
	private final Path rootLocation;

	@Autowired
	public FileUploadController(IStorageService storageService, final StorageProperties properties) {
		this.storageService = storageService;
		this.rootLocation = Paths.get(properties.getLocation());
	}

	@RequestMapping(method = { RequestMethod.GET, RequestMethod.POST })
	public Object index(HttpServletRequest req, @RequestParam(name = "file", required = false) MultipartFile file) {
		if (req.getMethod().equalsIgnoreCase("get")) {
			return "uploadForm";
		}
		final Map<String, Object> hashMap = new HashMap<>();
		try {
			// store file to upload-dir
			storageService.store(file);
			String filePath = rootLocation + "/" + file.getOriginalFilename();
			// read all lines to String
			String text = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
			// delete stored file
			storageService.delete(Paths.get(filePath));
			if ("Count words".equals(req.getParameter("button"))) {
				// pressed button "Count words"
				countWords(hashMap, text);
			} else {
				// pressed button "Validate brackets"
				validateBrackets(hashMap, text);
			}
		} catch (StorageException | IOException e) {
			hashMap.put("message", e.getMessage());
            LOGGER.warn(e.getMessage());
		}
		return new ModelAndView("uploadForm", hashMap);
	}

	private void countWords(Map<String, Object> hashMap, final String text) {
		try {
			// read all exclusion words to list
			EXCLUSION = Files.newBufferedReader(Paths.get(EXCLUSIONS_PATH)).lines().map(String::toLowerCase)
					.collect(Collectors.toList());
		} catch (IOException e) {
			hashMap.put("message", e.getMessage());
			LOGGER.warn(e.getMessage());
		}

		final Map<String, Integer> unique = Stream.of(text.toLowerCase().split(SPLIT_SYMBOLS))// split text
				.filter(p -> !(StringUtils.isEmpty(p) || EXCLUSION.contains(p))).map(elem -> new String(elem))// words filter
				.collect(Collectors.groupingBy(Function.identity(), Collectors.summingInt(e -> 1)));// count repeatable
																									// words
		// sort by count and limit(10)
		final Map<String, Integer> sortedMap = unique.entrySet().stream()
				.sorted(Map.Entry.comparingByValue((e1, e2) -> e1.equals(e2) ? 0 : ((e1 < e2) ? 1 : -1))).limit(10)
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

		hashMap.put("repeatableWords", sortedMap);
	}

	private void validateBrackets(Map<String, Object> hashMap, final String text) {
		final BracketValidator bracketValidator = new BracketValidator();
		//text to list only brackets
		final List<Character> brackets = text.chars()
				.mapToObj(x -> (char) x)
				.filter(x -> BRACKETS.contains(x.toString())).collect(Collectors.toList());
		if (bracketValidator.validate(brackets)) {
			hashMap.put("message", "correct");
		} else {
			hashMap.put("message", "incorrect");
		}
	}
}
