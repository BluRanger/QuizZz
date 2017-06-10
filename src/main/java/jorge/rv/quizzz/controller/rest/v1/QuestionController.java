package jorge.rv.quizzz.controller.rest.v1;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jorge.rv.quizzz.exceptions.ResourceUnavailableException;
import jorge.rv.quizzz.exceptions.UnauthorizedActionException;
import jorge.rv.quizzz.model.Answer;
import jorge.rv.quizzz.model.Question;
import jorge.rv.quizzz.model.Quiz;
import jorge.rv.quizzz.model.UserInfo;
import jorge.rv.quizzz.service.QuestionService;
import jorge.rv.quizzz.service.QuizService;

@RestController
@RequestMapping(QuestionController.ROOT_MAPPING )
public class QuestionController {
	
	public final static String ROOT_MAPPING = "/questions";
	
	@Autowired
	private QuestionService questionService;
	
	@Autowired
	private QuizService quizService;
	
	@RequestMapping(value = "", method = RequestMethod.POST)
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<?> save(@AuthenticationPrincipal UserInfo user, 
							@Valid Question question, 
							BindingResult result, 
							@RequestParam Long quiz_id) {
		
		if (result.hasErrors()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result.getAllErrors());
		}
		
		try {
			Quiz quiz = quizService.find(quiz_id);
			question.setQuiz(quiz);
			Question newQuestion =  questionService.save(question, user);
			return ResponseEntity.status(HttpStatus.CREATED).body(newQuestion);
		} catch (UnauthorizedActionException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		} catch (ResourceUnavailableException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}
	}
	
	@RequestMapping(value = "/{question_id}", method = RequestMethod.GET)
	@PreAuthorize("permitAll")
	public ResponseEntity<?> find(@PathVariable Long question_id) {
		try {
			Question question = questionService.find(question_id);
			return ResponseEntity.status(HttpStatus.OK).body(question);
		} catch (ResourceUnavailableException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}
	}
	
	@RequestMapping(value = "/{question_id}", method = RequestMethod.POST)
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<?> update(@AuthenticationPrincipal UserInfo user, 
							@PathVariable Long question_id, 
							@Valid Question question, 
							BindingResult result) {
		if (result.hasErrors()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result.getAllErrors());
		}
		
		try {
			Question updatedQuestion = questionService.update(question_id, question, user);
			return ResponseEntity.status(HttpStatus.OK).body(updatedQuestion);
		} catch (UnauthorizedActionException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		} catch (ResourceUnavailableException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}
		
	}
	
	@RequestMapping(value = "/{question_id}", method = RequestMethod.DELETE)
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<?> delete(@AuthenticationPrincipal UserInfo user, @PathVariable Long question_id) {
		try {
			questionService.delete(question_id, user);
			return ResponseEntity.status(HttpStatus.OK).build();
		} catch (UnauthorizedActionException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		} catch (ResourceUnavailableException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}
	}
	
	@RequestMapping(value = "/{question_id}/answers", method = RequestMethod.GET)
	@PreAuthorize("permitAll")
	public ResponseEntity<?> findAnswers(@PathVariable Long question_id) {
		try {
			List<Answer> answers = questionService.findAnswersByQuestion(question_id);
			return ResponseEntity.status(HttpStatus.OK).body(answers);
		} catch (ResourceUnavailableException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}
	}

}
