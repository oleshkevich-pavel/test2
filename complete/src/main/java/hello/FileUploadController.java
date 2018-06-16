package hello;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import hello.storage.StorageException;
import hello.storage.StorageProperties;
import hello.storage.IStorageService;

@Controller
@RequestMapping(value = "/")
public class FileUploadController {

	private final IStorageService storageService;
	public static final List<String> EXCLUDE = 
		    Collections.unmodifiableList(Arrays.asList("в", "а"));
	private final Path rootLocation;

	@Autowired
	public FileUploadController(IStorageService storageService, final StorageProperties properties) {
		this.storageService = storageService;
		this.rootLocation = Paths.get(properties.getLocation());
	}

	@RequestMapping(method = { RequestMethod.GET, RequestMethod.POST })
	public Object index(HttpServletRequest req, @RequestParam(name = "file", required = false) MultipartFile file) {
		if (req.getMethod().equalsIgnoreCase("get")) {
/*			String password = "password123[]в {в}(ва)(а))(}{ва}{}adlkaf}{}";

			password.chars()// IntStream
					.mapToObj(x -> (char) x)// Stream<Character>
					.filter(x -> ")}]({[".contains(x.toString())).forEach(System.out::println);

			Stream.of(password.toLowerCase().split("[^a-zA-Zа-яА-Я]")).map(elem -> new String(elem))
					.collect(Collectors.toList()).forEach(System.out::print);

			Map<String, Integer> collect = Stream.of(password.toLowerCase().split("[^a-zA-Zа-яА-Я]"))
					.filter(p -> !(StringUtils.isEmpty(p)||EXCLUDE.contains(p)))
					.map(elem -> new String(elem))
					.collect(Collectors.groupingBy(Function.identity(), Collectors.summingInt(e -> 1)));

			System.out.println(collect);
			final Map<String, Integer> sortedMap = collect.entrySet().stream()
					.sorted(Map.Entry.comparingByValue((e1, e2) -> e1.equals(e2) ? 0 : ((e1 < e2) ? 1 : -1))).limit(10)
					.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

			System.out.println(sortedMap);*/
			return "uploadForm";
		}
		final Map<String, Object> hashMap = new HashMap<>();
		try {
			storageService.store(file);
			String filePath = rootLocation + "/" + file.getOriginalFilename();
			String text = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
			storageService.delete(Paths.get(filePath));
			if ("Count words".equals(req.getParameter("button"))) {
				countWords(hashMap, text);
			} else {
				validateBrackets(hashMap, text);
			}
		} catch (StorageException | IOException e) {
			hashMap.put("message", e.getMessage());
		}
		return new ModelAndView("uploadForm", hashMap);
	}

	private void countWords(Map<String, Object> hashMap, final String text) {
		Map<String, Integer> unique = new LinkedHashMap<String, Integer>();

		for (String word : text.toLowerCase().split("[^a-zA-Zа-яА-Я]")) {
			// какой-то символ попадает пустой
			if (!StringUtils.isEmpty(word)) {
				if (unique.get(word) == null)
					unique.put(word, 1);
				else
					unique.put(word, unique.get(word) + 1);
			}
		}

		System.out.println();
		unique.forEach((k, v) -> System.out.println("key: " + k + " value:" + v));
		// TODO delete
		final Map<String, Integer> sortedMap = unique.entrySet().stream()
				.sorted(Map.Entry.comparingByValue((e1, e2) -> e1.equals(e2) ? 0 : ((e1 < e2) ? 1 : -1))).limit(10)
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		hashMap.put("repeatableWords", sortedMap);
	}

	private void validateBrackets(Map<String, Object> hashMap, final String text) {
		BracketValidator bracketValidator = new BracketValidator();
		if (bracketValidator.validate(text)) {
			hashMap.put("message", "correct");
		} else {
			hashMap.put("message", "incorrect");
		}
	}
}
