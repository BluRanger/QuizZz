package jorge.rv.QuizZz.unitTests.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import jorge.rv.quizzz.exceptions.QuizZzException;
import jorge.rv.quizzz.exceptions.ResourceUnavailableException;
import jorge.rv.quizzz.exceptions.UnauthorizedActionException;
import jorge.rv.quizzz.model.Question;
import jorge.rv.quizzz.model.Quiz;
import jorge.rv.quizzz.model.UserInfo;
import jorge.rv.quizzz.repository.QuizRepository;
import jorge.rv.quizzz.service.AccessControlService;
import jorge.rv.quizzz.service.QuizService;
import jorge.rv.quizzz.service.QuizServiceImpl;

public class QuizServiceTests {
	
	private static final int DEFAULT_PAGE_SIZE = 5;
	private static final Pageable pageable = createDefaultPage();

	QuizService service;
	
	// Mocks
	QuizRepository quizRepository;
	AccessControlService accessControlService;
	
	UserInfo user = new UserInfo();
	Quiz quiz = new Quiz();
	
	@Before
	public void before() {
		quizRepository = mock(QuizRepository.class);
		accessControlService = mock(AccessControlService.class);
		service = new QuizServiceImpl(quizRepository, accessControlService);
		
		user.setId(1l);
		
		quiz.setCreatedBy(user);
		quiz.setId(1l);
		quiz.setId(2l);
		
	}
	
	// Save
	
	@Test
	public void testSaveQuiz() {
		service.save(quiz, user);
		verify(quizRepository, times(1)).save(quiz);
	}
	
	// FindAll
	
	@Test
	public void findAllQuizzesEmpty() {
		when(quizRepository.findAll(pageable)).thenReturn(new PageImpl<>(new ArrayList<Quiz>()));
		
		List<Quiz> result = service.findAll(pageable).getContent();
		
		verify(quizRepository, times(1)).findAll(pageable);
		assertEquals(0, result.size());
	}
	
	@Test
	public void findAllQuizzesWithContent() {
		ArrayList<Quiz> q = new ArrayList<>();
		q.add(quiz);
		q.add(new Quiz());
		when(quizRepository.findAll(pageable)).thenReturn(new PageImpl<>(q));
		
		List<Quiz> result = service.findAll(pageable).getContent();
		
		verify(quizRepository, times(1)).findAll(pageable);
		assertEquals(2, result.size());
	}
	
	// Find
	
	@Test
	public void findExistingQuiz() throws ResourceUnavailableException {
		when(quizRepository.findOne(quiz.getId())).thenReturn(quiz);
		
		Quiz returned = service.find(quiz.getId());
		
		verify(quizRepository, times(1)).findOne(quiz.getId());
		assertNotNull(returned);
		assertEquals(quiz.getId(), returned.getId());
	}
	
	@Test(expected = ResourceUnavailableException.class)
	public void findNonExistingQuiz() throws ResourceUnavailableException {
		when(quizRepository.findOne(quiz.getId())).thenReturn(null);
		
		service.find(quiz.getId());
		
		verify(quizRepository, times(1)).findOne(quiz.getId());
	}
	
	// Update
	
	@Test
	public void testUpdateShouldUpdate() throws QuizZzException {
		quiz.setName("test");
		
		when(quizRepository.findOne(quiz.getId())).thenReturn(quiz);
		when(quizRepository.save(quiz)).thenReturn(quiz);
		Quiz returned = service.update(quiz.getId(), quiz, quiz.getCreatedBy());
		
		verify(quizRepository, times(1)).save(quiz);
		assertTrue(quiz.getName().equals(returned.getName()));
	}
	
	@Test(expected = ResourceUnavailableException.class)
	public void testUpdateUnexistentQuiz() throws QuizZzException {
		quiz.setName("test");
		
		when(quizRepository.findOne(quiz.getId())).thenReturn(null);
		
		service.update(quiz.getId(), quiz, user);
	}
	
	@Test(expected = UnauthorizedActionException.class)
	public void testUpdateFromWrongUser() throws QuizZzException {
		quiz.setName("test");
		
		when(quizRepository.findOne(quiz.getId())).thenReturn(quiz);
		doThrow(new UnauthorizedActionException())
			.when(accessControlService).checkUserPriviledges(user, quiz);
		
		service.update(quiz.getId(), quiz, user);
	}
	
	// Delete

	@Test
	public void testDeleteShouldDelete() throws QuizZzException {
		when(quizRepository.findOne(quiz.getId())).thenReturn(quiz);
		service.delete(quiz.getId(), quiz.getCreatedBy());
		
		verify(quizRepository, times(1)).delete(quiz);
	}
	
	@Test(expected = ResourceUnavailableException.class)
	public void testDeleteUnexistentQuiz() throws QuizZzException {
		when(quizRepository.findOne(quiz.getId())).thenReturn(null);
		
		service.delete(quiz.getId(), user);
	}
	
	@Test(expected = UnauthorizedActionException.class)
	public void testDeleteFromWrongUser() throws QuizZzException {
		when(quizRepository.findOne(quiz.getId())).thenReturn(quiz);
		doThrow(new UnauthorizedActionException())
			.when(accessControlService).checkUserPriviledges(user, quiz);
		
		service.delete(quiz.getId(), user);
	}
	
	// FindQuestionsById
	
	@Test
	public void testFindQuestionsByQuizWithAvailableQuizAndEmptyQuestions() throws ResourceUnavailableException {
		quiz.setQuestions(new ArrayList<Question>());
		when(quizRepository.findOne(quiz.getId())).thenReturn(quiz);
		
		List<Question> questions = service.findQuestionsByQuiz(quiz.getId());
		
		assertEquals(0, questions.size());
	}

	@Test
	public void testFindQuestionsByQuizWithAvailableQuizAndQuestionsAvailable() throws ResourceUnavailableException {
		List<Question> mockedQuestions = new ArrayList<>();
		mockedQuestions.add(new Question());
		mockedQuestions.add(new Question());
		mockedQuestions.add(new Question());
		
		quiz.setQuestions(mockedQuestions);
		when(quizRepository.findOne(quiz.getId())).thenReturn(quiz);
		
		List<Question> questions = service.findQuestionsByQuiz(quiz.getId());
		
		assertEquals(3, questions.size());
	}
	
	@Test(expected = ResourceUnavailableException.class)
	public void testFindQuestionsByQuizWithInvalidQuizID() throws ResourceUnavailableException {
		when(quizRepository.findOne(quiz.getId())).thenReturn(null);
		
		service.findQuestionsByQuiz(quiz.getId());
	}
	
	private static Pageable createDefaultPage() {
		return new PageRequest(0, DEFAULT_PAGE_SIZE);
	}
	
}
