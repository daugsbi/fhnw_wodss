package ch.fhnw.wodss.integration;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ch.fhnw.wodss.domain.Board;
import ch.fhnw.wodss.domain.BoardFactory;
import ch.fhnw.wodss.domain.User;
import ch.fhnw.wodss.security.Token;
import ch.fhnw.wodss.service.BoardService;
import ch.fhnw.wodss.service.UserService;

public class BoardIntTest extends AbstractIntegrationTest {
	
	private static final Logger LOG = LoggerFactory.getLogger(BoardIntTest.class);

	@Autowired
	private BoardService boardService;

	@Autowired
	private UserService userService;

	@SuppressWarnings("unchecked")
	@Test
	public void testCreatBoard() throws Exception {

		JSONObject json = new JSONObject();
		json.put("email", "hans.muster@fhnw.ch");
		json.put("password", "password");

		// REQUEST TOKEN
		Token token = doPost("http://localhost:8080/token", null, json, Token.class);

		// CREATE
		json.clear();
		json.put("title", "TestBoard");

		Board board = doPost("http://localhost:8080/board", token, json, Board.class);
		Assert.assertNotNull(board.getId().intValue());
		Board boardFromDb = boardService.getById(board.getId());
		Assert.assertEquals("TestBoard", boardFromDb.getTitle());

		// READ
		board = doGet("http://localhost:8080/board/{0}", token, Board.class, boardFromDb.getId());
		Assert.assertNotNull(board);
		Assert.assertEquals("TestBoard", board.getTitle());
		Assert.assertNotNull(board.getId().intValue());
		
		LOG.debug(objectMapper.writeValueAsString(board));

	}

	/**
	 * Tests getting all boards where user is owner or has been invited.
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testGetBoards() throws Exception {
		JSONObject json = new JSONObject();

		// CREATE / REGISTER
		json.put("name", "TestUserA");
		json.put("email", "emailA@fhnw.ch");
		json.put("password", "password");

		User user = doPost("http://localhost:8080/user", null, json, User.class);
		User userFromDb = userService.getById(user.getId());
		Assert.assertEquals("TestUserA", userFromDb.getName());
		Assert.assertEquals("emailA@fhnw.ch", userFromDb.getEmail());
		Assert.assertNotNull(user.getId());

		json = new JSONObject();

		// CREATE / REGISTER
		json.put("name", "TestUserB");
		json.put("email", "emailB@fhnw.ch");
		json.put("password", "password2");

		User user2 = doPost("http://localhost:8080/user", null, json, User.class);
		User userFromDb2 = userService.getById(user2.getId());
		Assert.assertEquals("TestUserB", userFromDb2.getName());
		Assert.assertEquals("emailB@fhnw.ch", userFromDb2.getEmail());
		Assert.assertNotNull(user.getId());

		// VALIDATE EMAIL ADDRESS
		json.put("validationCode", userFromDb2.getLoginData().getValidationCode());
		Boolean success = doPut("http://localhost:8080/user/{0}/logindata", null, json, Boolean.class,
				userFromDb2.getId());
		Assert.assertTrue(success);
		userFromDb2 = userService.getById(user2.getId());
		Assert.assertTrue(userFromDb2.getLoginData().isValidated());

		// REQUEST TOKEN
		Token token = doPost("http://localhost:8080/token", null, json, Token.class);

		Board board1 = BoardFactory.getInstance().createBoard("Board1", user);
		board1 = boardService.saveBoard(board1);
		Assert.assertNotNull(board1.getId());

		Board board2 = BoardFactory.getInstance().createBoard("Board2", user2);
		board2 = boardService.saveBoard(board2);
		Assert.assertNotNull(board2.getId());

		Board board3 = BoardFactory.getInstance().createBoard("Board3", user2);
		board3 = boardService.saveBoard(board3);
		Assert.assertNotNull(board3.getId());

		Board[] boards = doGet("http://localhost:8080/boards", token, Board[].class);

		List<Board> boardList = Arrays.asList(boards);

		Assert.assertTrue(boardList.contains(board2));
		Assert.assertTrue(boardList.contains(board3));
	}

	/**
	 * Tests getting the board where user is owner or has been invited. If the
	 * user has not been subscribed to this board, the user in not authorized to
	 * get this board.
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testGetBoard() throws Exception {
		JSONObject json = new JSONObject();

		// CREATE / REGISTER
		json.put("name", "TestUser3");
		json.put("email", "email3@fhnw.ch");
		json.put("password", "password");

		User user = doPost("http://localhost:8080/user", null, json, User.class);
		User userFromDb = userService.getById(user.getId());
		Assert.assertEquals("TestUser3", userFromDb.getName());
		Assert.assertEquals("email3@fhnw.ch", userFromDb.getEmail());
		Assert.assertNotNull(user.getId());

		json = new JSONObject();

		// CREATE / REGISTER
		json.put("name", "TestUser4");
		json.put("email", "email4@fhnw.ch");
		json.put("password", "password2");

		User user2 = doPost("http://localhost:8080/user", null, json, User.class);
		User userFromDb2 = userService.getById(user2.getId());
		Assert.assertEquals("TestUser4", userFromDb2.getName());
		Assert.assertEquals("email4@fhnw.ch", userFromDb2.getEmail());
		Assert.assertNotNull(user.getId());

		// VALIDATE EMAIL ADDRESS
		json.put("validationCode", userFromDb2.getLoginData().getValidationCode());
		Boolean success = doPut("http://localhost:8080/user/{0}/logindata", null, json, Boolean.class,
				userFromDb2.getId());
		Assert.assertTrue(success);
		userFromDb2 = userService.getById(user2.getId());
		Assert.assertTrue(userFromDb2.getLoginData().isValidated());

		// REQUEST TOKEN
		Token token = doPost("http://localhost:8080/token", null, json, Token.class);

		Board board1 = BoardFactory.getInstance().createBoard("Board1", user);
		board1 = boardService.saveBoard(board1);
		Assert.assertNotNull(board1.getId());

		Board board2 = BoardFactory.getInstance().createBoard("Board2", user2);
		board2 = boardService.saveBoard(board2);
		Assert.assertNotNull(board2.getId());

		Board board3 = BoardFactory.getInstance().createBoard("Board3", user2);
		board3 = boardService.saveBoard(board3);
		Assert.assertNotNull(board3.getId());

		try {
			board2 = doGet("http://localhost:8080/board/{0}", token, Board.class, board2.getId());
			board3 = doGet("http://localhost:8080/board/{0}", token, Board.class, board3.getId());
		} catch (IOException e) {
			Assert.fail();
		} catch (Exception e) {
			Assert.fail();
		}
		Assert.assertNotNull(board2);
		Assert.assertNotNull(board3);

		try {
			board1 = doGet("http://localhost:8080/board/{0}", token, Board.class, board1.getId());
		} catch (IOException e) {
		} catch (Exception e) {
			Assert.fail();
		}

	}

	/**
	 * Tests deleting a board. Only the board owner should have the possibility
	 * to delete the board.
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testDeleteBoard() throws Exception {
		JSONObject json = new JSONObject();

		// CREATE / REGISTER
		json.put("name", "TestUser5");
		json.put("email", "email5@fhnw.ch");
		json.put("password", "password");

		User user = doPost("http://localhost:8080/user", null, json, User.class);
		User userFromDb = userService.getById(user.getId());
		Assert.assertEquals("TestUser5", userFromDb.getName());
		Assert.assertEquals("email5@fhnw.ch", userFromDb.getEmail());
		Assert.assertNotNull(user.getId());

		json = new JSONObject();

		// CREATE / REGISTER
		json.put("name", "TestUser6");
		json.put("email", "email6@fhnw.ch");
		json.put("password", "password2");

		User user2 = doPost("http://localhost:8080/user", null, json, User.class);
		User userFromDb2 = userService.getById(user2.getId());
		Assert.assertEquals("TestUser6", userFromDb2.getName());
		Assert.assertEquals("email6@fhnw.ch", userFromDb2.getEmail());
		Assert.assertNotNull(user.getId());

		// VALIDATE EMAIL ADDRESS
		json.put("validationCode", userFromDb2.getLoginData().getValidationCode());
		Boolean success = doPut("http://localhost:8080/user/{0}/logindata", null, json, Boolean.class,
				userFromDb2.getId());
		Assert.assertTrue(success);
		userFromDb2 = userService.getById(user2.getId());
		Assert.assertTrue(userFromDb2.getLoginData().isValidated());

		// REQUEST TOKEN
		Token token = doPost("http://localhost:8080/token", null, json, Token.class);

		Board board1 = BoardFactory.getInstance().createBoard("Board1", user);
		board1 = boardService.saveBoard(board1);
		Assert.assertNotNull(board1.getId());

		Board board2 = BoardFactory.getInstance().createBoard("Board2", user2);
		board2.addUser(user);
		board2 = boardService.saveBoard(board2);
		Assert.assertNotNull(board2.getId());
		Assert.assertTrue(board2.getUsers().contains(user));

		Board board3 = BoardFactory.getInstance().createBoard("Board3", user2);
		board3.addUser(user);
		board3 = boardService.saveBoard(board3);
		Assert.assertNotNull(board3.getId());
		Assert.assertTrue(board3.getUsers().contains(user));

		boolean delboard2 = false;
		boolean delboard3 = false;
		try {
			delboard2 = doDelete("http://localhost:8080/board/{0}", token, Boolean.class, board2.getId());
			delboard3 = doDelete("http://localhost:8080/board/{0}", token, Boolean.class, board3.getId());
		} catch (IOException e) {
			Assert.fail();
		} catch (Exception e) {
			Assert.fail();
		}
		Assert.assertTrue(delboard2);
		Assert.assertTrue(delboard3);

		boolean delboard1 = false;
		try {
			delboard1 = doDelete("http://localhost:8080/board/{0}", token, Boolean.class, board1.getId());
		} catch (IOException e) {
		} catch (Exception e) {
			Assert.fail();
		}
		Assert.assertFalse(delboard1);

	}
	
	/**
	 * Tests modifying a board. Only the board owner should have the possibility
	 * to modify the board.
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testModifyBoard() throws Exception {
		JSONObject json = new JSONObject();

		// CREATE / REGISTER
		json.put("name", "TestUser7");
		json.put("email", "email7@fhnw.ch");
		json.put("password", "password");

		User user = doPost("http://localhost:8080/user", null, json, User.class);
		User userFromDb = userService.getById(user.getId());
		Assert.assertEquals("TestUser7", userFromDb.getName());
		Assert.assertEquals("email7@fhnw.ch", userFromDb.getEmail());
		Assert.assertNotNull(user.getId());

		json = new JSONObject();

		// CREATE / REGISTER
		json.put("name", "TestUser8");
		json.put("email", "email8@fhnw.ch");
		json.put("password", "password2");

		User user2 = doPost("http://localhost:8080/user", null, json, User.class);
		User userFromDb2 = userService.getById(user2.getId());
		Assert.assertEquals("TestUser8", userFromDb2.getName());
		Assert.assertEquals("email8@fhnw.ch", userFromDb2.getEmail());
		Assert.assertNotNull(user.getId());

		// VALIDATE EMAIL ADDRESS
		json.put("validationCode", userFromDb2.getLoginData().getValidationCode());
		Boolean success = doPut("http://localhost:8080/user/{0}/logindata", null, json, Boolean.class,
				userFromDb2.getId());
		Assert.assertTrue(success);
		userFromDb2 = userService.getById(user2.getId());
		Assert.assertTrue(userFromDb2.getLoginData().isValidated());

		// REQUEST TOKEN
		Token token = doPost("http://localhost:8080/token", null, json, Token.class);

		Board board1 = BoardFactory.getInstance().createBoard("Board1", user);
		board1 = boardService.saveBoard(board1);
		Assert.assertNotNull(board1.getId());

		Board board2 = BoardFactory.getInstance().createBoard("Board2", user2);
		board2.addUser(user);
		board2 = boardService.saveBoard(board2);
		Assert.assertNotNull(board2.getId());
		Assert.assertTrue(board2.getUsers().contains(user));

		Board board3 = BoardFactory.getInstance().createBoard("Board3", user2);
		board3.addUser(user);
		board3 = boardService.saveBoard(board3);
		Assert.assertNotNull(board3.getId());
		Assert.assertTrue(board3.getUsers().contains(user));

		board2.setTitle("OtherTitle2");
		board3.setTitle("OtherTitle3");
		
		JSONParser parser = new JSONParser();
		Object board2json = parser.parse(objectMapper.writeValueAsString(board2));
		Object board3json = parser.parse(objectMapper.writeValueAsString(board3));
		
		try {
			board2 = doPut("http://localhost:8080/board/{0}", token, board2json, Board.class, board2.getId());
			board3 = doPut("http://localhost:8080/board/{0}", token, board3json, Board.class, board3.getId());
		} catch (IOException e) {
			Assert.fail();
		} catch (Exception e) {
			Assert.fail();
		}
		Assert.assertEquals("OtherTitle2", board2.getTitle());
		Assert.assertEquals("OtherTitle3", board3.getTitle());

		board3.setTitle("OtherTitle1");
		Object board1json = parser.parse(objectMapper.writeValueAsString(board1));
		try {
			board3 = doPut("http://localhost:8080/board/{0}", token, board1json, Board.class, board1.getId());
		} catch (IOException e) {
		} catch (Exception e) {
			Assert.fail();
		}
		Assert.assertEquals("Board1", board1.getTitle());

	}
	
	/**
	 * Tests creating a board with unregistered users
	 * @throws Exception 
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testCreateBoardWithUnregisteredUsers() throws Exception{
		JSONObject json = new JSONObject();
		json.put("email", "hans.muster@fhnw.ch");
		json.put("password", "password");

		// REQUEST TOKEN
		Token token = doPost("http://localhost:8080/token", null, json, Token.class);

		// CREATE
		json.clear();
		json.put("title", "TestBoard");
	
		JSONObject user1 = new JSONObject();
		user1.put("email", "ama.zon@amazon.com");
		
		JSONObject user2 = new JSONObject();
		user2.put("email", "fh.nw@fhnw.ch");
		
		JSONArray jArray = new JSONArray();
		jArray.add(user1);
		jArray.add(user2);
		
		json.put("users", jArray);
		
		Board board = objectMapper.readValue(json.toJSONString(), Board.class);
		Assert.assertEquals(2, board.getUsers().size());

		board = doPost("http://localhost:8080/board", token, json, Board.class);
		Assert.assertNotNull(board.getId().intValue());
		Board boardFromDb = boardService.getById(board.getId());
		Assert.assertEquals("TestBoard", boardFromDb.getTitle());
		Assert.assertEquals(3, boardFromDb.getUsers().size());

		// READ
		board = doGet("http://localhost:8080/board/{0}", token, Board.class, boardFromDb.getId());
		Assert.assertNotNull(board);
		Assert.assertEquals("TestBoard", board.getTitle());
		Assert.assertNotNull(board.getId().intValue());
		
		// LET UNREGISTER USER REGISTER
		json = new JSONObject();

		// CREATE / REGISTER
		json.put("name", "AMA.ZON");
		json.put("email", "ama.zon@amazon.com");
		json.put("password", "password");

		User user = doPost("http://localhost:8080/user", null, json, User.class);
		User userFromDb = userService.getById(user.getId());
		Assert.assertEquals("AMA.ZON", userFromDb.getName());
		Assert.assertEquals("ama.zon@amazon.com", userFromDb.getEmail());
		Assert.assertNotNull(user.getId());

		// VALIDATE EMAIL ADDRESS
		json.put("validationCode", userFromDb.getLoginData().getValidationCode());
		Boolean success = doPut("http://localhost:8080/user/{0}/logindata", null, json, Boolean.class,
				userFromDb.getId());
		Assert.assertTrue(success);
		userFromDb = userService.getById(user.getId());
		Assert.assertTrue(userFromDb.getLoginData().isValidated());

		// REQUEST TOKEN
		token = doPost("http://localhost:8080/token", null, json, Token.class);

		// READ
		user = doGet("http://localhost:8080/user/{0}", token, User.class, userFromDb.getId());
		Assert.assertNotNull(user);
		Assert.assertEquals("AMA.ZON", user.getName());
	
		// TEST BOARDS NEW USER IS IN
		board = doGet("http://localhost:8080/board/{0}", token, Board.class, boardFromDb.getId());
		Assert.assertTrue(board.getUsers().contains(user));

	}
	
	
}