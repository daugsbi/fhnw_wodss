package ch.fhnw.wodss.integration;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;

import ch.fhnw.wodss.domain.Attachment;
import ch.fhnw.wodss.domain.AttachmentFactory;
import ch.fhnw.wodss.domain.Board;
import ch.fhnw.wodss.domain.BoardFactory;
import ch.fhnw.wodss.domain.Task;
import ch.fhnw.wodss.domain.TaskFactory;
import ch.fhnw.wodss.domain.User;
import ch.fhnw.wodss.security.Token;
import ch.fhnw.wodss.service.AttachmentService;
import ch.fhnw.wodss.service.BoardService;
import ch.fhnw.wodss.service.TaskService;
import ch.fhnw.wodss.service.UserService;

public class AttachmentIntTest extends AbstractIntegrationTest {

	@Autowired
	private BoardService boardService;

	@Autowired
	private UserService userService;

	@Autowired
	private TaskService taskService;

	@Autowired
	private AttachmentService attachmentService;

	private File attachmentFile;

	@Before
	public void setupAttachmentFile() throws URISyntaxException {
		attachmentFile = new File(AttachmentIntTest.class.getClassLoader()
				.getResource("ch/fhnw/wodss/integration/Trello_1.1.pdf").toURI());
	}

	/**
	 * Tests getting an attachment.
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	@Ignore
	public void testGetAttachment() throws Exception {
		JSONObject jsonUser1 = new JSONObject();

		// CREATE / REGISTER
		jsonUser1.put("name", "TestUser24");
		jsonUser1.put("email", "email24@fhnw.ch");
		jsonUser1.put("password", "password");

		User user1 = doPost("http://localhost:8080/user", null, jsonUser1, User.class);
		User userFromDb1 = userService.getById(user1.getId());
		Assert.assertEquals("TestUser24", userFromDb1.getName());
		Assert.assertEquals("email24@fhnw.ch", userFromDb1.getEmail());
		Assert.assertNotNull(user1.getId());

		JSONObject jsonUser2 = new JSONObject();

		// CREATE / REGISTER
		jsonUser2.put("name", "TestUser25");
		jsonUser2.put("email", "email25@fhnw.ch");
		jsonUser2.put("password", "password2");

		User user2 = doPost("http://localhost:8080/user", null, jsonUser2, User.class);
		User userFromDb2 = userService.getById(user2.getId());
		Assert.assertEquals("TestUser25", userFromDb2.getName());
		Assert.assertEquals("email25@fhnw.ch", userFromDb2.getEmail());
		Assert.assertNotNull(user2.getId());

		// VALIDATE EMAIL ADDRESS
		JSONObject json = new JSONObject();
		json.put("validationCode", userFromDb1.getLoginData().getValidationCode());
		Boolean success = doPut("http://localhost:8080/user/{0}/logindata", null, json, Boolean.class,
				userFromDb1.getId());
		Assert.assertTrue(success);
		userFromDb1 = userService.getById(user1.getId());
		Assert.assertTrue(userFromDb1.getLoginData().isValidated());
		json.put("validationCode", userFromDb2.getLoginData().getValidationCode());
		success = doPut("http://localhost:8080/user/{0}/logindata", null, json, Boolean.class, userFromDb2.getId());
		Assert.assertTrue(success);
		userFromDb2 = userService.getById(user2.getId());
		Assert.assertTrue(userFromDb2.getLoginData().isValidated());

		// REQUEST TOKEN
		Token token1 = doPost("http://localhost:8080/token", null, jsonUser1, Token.class);
		Token token2 = doPost("http://localhost:8080/token", null, jsonUser2, Token.class);

		Board board1 = BoardFactory.getInstance().createBoard("Board1", user1);
		board1 = boardService.saveBoard(board1);
		Assert.assertNotNull(board1.getId());

		Board board2 = BoardFactory.getInstance().createBoard("Board2", user2);
		board2 = boardService.saveBoard(board2);
		Assert.assertNotNull(board2.getId());

		Board board3 = BoardFactory.getInstance().createBoard("Board3", user2);
		board3 = boardService.saveBoard(board3);
		Assert.assertNotNull(board3.getId());

		Task task1 = TaskFactory.getInstance().createTask(board1, "Task1");
		JSONParser parser = new JSONParser();
		JSONObject taskjson = (JSONObject) parser.parse(objectMapper.writeValueAsString(task1));

		task1 = doMulitPartPostTask("http://localhost:8080/task", token1, taskjson, Arrays.asList(attachmentFile));
		Assert.assertNotNull(task1.getId());
		Assert.assertNotNull(task1.getAttachments());

		try {
			doGet("http://localhost:8080/attachment/{0}", token1, Object.class, task1.getAttachments().get(0).getId());
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail();
		} catch (Exception e) {
			Assert.fail();
		}

		try {
			doGet("http://localhost:8080/attachment/{0}", token2, InputStreamResource.class, task1.getAttachments().get(0).getId());
		} catch (IOException e) {
			Assert.fail();
		} catch (Exception e) {
			Assert.fail();
		}

	}

	/**
	 * Tests deleting an attachment. This should be return an attachment only if
	 * an attachment exists and when the user is subscribed to the board that
	 * contains the task with this attachment.
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testDeleteAttachment() throws Exception {
		JSONObject jsonUser1 = new JSONObject();

		// CREATE / REGISTER
		jsonUser1.put("name", "TestUser26");
		jsonUser1.put("email", "email26@fhnw.ch");
		jsonUser1.put("password", "password");

		User user1 = doPost("http://localhost:8080/user", null, jsonUser1, User.class);
		User userFromDb1 = userService.getById(user1.getId());
		Assert.assertEquals("TestUser26", userFromDb1.getName());
		Assert.assertEquals("email26@fhnw.ch", userFromDb1.getEmail());
		Assert.assertNotNull(user1.getId());

		JSONObject jsonUser2 = new JSONObject();

		// CREATE / REGISTER
		jsonUser2.put("name", "TestUser27");
		jsonUser2.put("email", "email27@fhnw.ch");
		jsonUser2.put("password", "password2");

		User user2 = doPost("http://localhost:8080/user", null, jsonUser2, User.class);
		User userFromDb2 = userService.getById(user2.getId());
		Assert.assertEquals("TestUser27", userFromDb2.getName());
		Assert.assertEquals("email27@fhnw.ch", userFromDb2.getEmail());
		Assert.assertNotNull(user2.getId());

		// VALIDATE EMAIL ADDRESS
		JSONObject json = new JSONObject();
		json.put("validationCode", userFromDb1.getLoginData().getValidationCode());
		Boolean success = doPut("http://localhost:8080/user/{0}/logindata", null, json, Boolean.class,
				userFromDb1.getId());
		Assert.assertTrue(success);
		userFromDb1 = userService.getById(user1.getId());
		Assert.assertTrue(userFromDb1.getLoginData().isValidated());
		json.put("validationCode", userFromDb2.getLoginData().getValidationCode());
		success = doPut("http://localhost:8080/user/{0}/logindata", null, json, Boolean.class, userFromDb2.getId());
		Assert.assertTrue(success);
		userFromDb2 = userService.getById(user2.getId());
		Assert.assertTrue(userFromDb2.getLoginData().isValidated());

		// REQUEST TOKEN
		Token token1 = doPost("http://localhost:8080/token", null, jsonUser1, Token.class);
		Token token2 = doPost("http://localhost:8080/token", null, jsonUser2, Token.class);

		Board board1 = BoardFactory.getInstance().createBoard("Board1", user1);
		board1 = boardService.saveBoard(board1);
		Assert.assertNotNull(board1.getId());

		Board board2 = BoardFactory.getInstance().createBoard("Board2", user2);
		board2 = boardService.saveBoard(board2);
		Assert.assertNotNull(board2.getId());

		Board board3 = BoardFactory.getInstance().createBoard("Board3", user2);
		board3 = boardService.saveBoard(board3);
		Assert.assertNotNull(board3.getId());

		Task task1 = TaskFactory.getInstance().createTask(board1, "Task1");

		Attachment attachment = AttachmentFactory.getInstance().createAttachment(task1, "pdf");
		success = attachmentService.saveAttachmentToFileSystem(attachment, attachmentFile);
		Assert.assertTrue(success);

		task1.getAttachments().add(attachment);
		task1 = taskService.saveTask(task1);
		Assert.assertNotNull(task1.getId());

		try {
			success = doDelete("http://localhost:8080/attachment/{0}", token2, Boolean.class, attachment.getId());
			Assert.fail();
		} catch (IOException e) {
		} catch (Exception e) {
			Assert.fail();
		}

		try {
			success = doDelete("http://localhost:8080/attachment/{0}", token1, Boolean.class, attachment.getId());
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail();
		} catch (Exception e) {
			Assert.fail();
		}

		Assert.assertFalse(attachment.getFile().exists());
	}

}
