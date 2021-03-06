package com.group5.definitions.restcontrollers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;
import com.group5.definitions.model.Answer;
import com.group5.definitions.model.Concept;
import com.group5.definitions.model.Justification;
import com.group5.definitions.model.Question;
import com.group5.definitions.services.AnswerService;
import com.group5.definitions.services.ConceptService;
import com.group5.definitions.services.JustificationService;
import com.group5.definitions.services.QuestionService;
import com.group5.definitions.usersession.UserSessionService;

@RestController
@RequestMapping("/api")
public class RestConceptControllerTeacher {

	private final int DEFAULT_SIZE = 10;
	@Autowired
	private ConceptService conceptService;
	@Autowired
	private JustificationService justificationService;
	@Autowired
	private AnswerService answerService;
	@Autowired
	private QuestionService questionService;
	@Autowired
	private UserSessionService userSession;

	@JsonView(Concept.Basic.class)
	@PutMapping("/concepts/{id}")
	public ResponseEntity<Concept> updateConcept(@PathVariable long id, @RequestBody Concept concept) {
		Concept oldConcept = conceptService.findById(id);
		if (oldConcept == null)
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		concept.setId(id);
		if (concept.getChapter() == null)
			concept.setChapter(oldConcept.getChapter());
		conceptService.save(concept);
		return new ResponseEntity<>(concept, HttpStatus.OK);
	}

	@JsonView(Justification.Basic.class)
	@PutMapping("/justifications/{justId}")
	public ResponseEntity<Justification> updateJustification(@PathVariable long justId,
			@RequestBody Justification justification) {
		Justification oldJust = justificationService.findById(justId);
		if (oldJust == null)
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		if (!oldJust.isMarked())
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		if (!justification.isValid() && (justification.getError() == null))
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		if (justification.isValid())
			justification.setError("");
		justification.setId(justId);
		justification.setMarked(true);
		justification.setAnswer(oldJust.getAnswer());
		justification.setUser(oldJust.getUser());
		justificationService.save(justification);
		return new ResponseEntity<>(justification, HttpStatus.OK);
	}

	@JsonView(Justification.Basic.class)
	@RequestMapping(value = "/justifications/{id}", method = RequestMethod.DELETE)
	public ResponseEntity<Justification> deleteJustification(@PathVariable long id) {
		Justification justification = justificationService.findById(id);
		if (justification.isMarked() && justification.getAnswer().countMarkedJustifications() > 1) {
			justificationService.deleteById(id);
			return new ResponseEntity<>(justification, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@JsonView(Answer.Basic.class)
	@PutMapping(value = "/answers/{id}")
	public ResponseEntity<Answer> modifyAnswer(@PathVariable Long id, @RequestBody Answer updatedAnswer) {
		Answer oldAnswer = answerService.getOne(id);
		if (oldAnswer != null) {
			updatedAnswer.setId(id);
			if (updatedAnswer.getConcept() == null) {
				updatedAnswer.setConcept(oldAnswer.getConcept());
			}
			answerService.save(updatedAnswer);
			// Needs more work (if answer is wrong, it needs a valid justification)
			return new ResponseEntity<>(updatedAnswer, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	// No need to test, this isn't going to work
	interface AnswerJustification extends Answer.Basic, Justification.Basic{}
	@JsonView(AnswerJustification.class)
	@DeleteMapping("/concepts/{conceptId}/mark/{answerId}")
	public ResponseEntity<Answer> markAnswer(@PathVariable long conceptId, @PathVariable long answerId,
			@RequestParam boolean correct, @RequestParam(required = false) String justificationTextNew,
			@RequestBody Answer answer) {
		answer.setMarked(true);
		answer.setCorrect(correct);
		answerService.save(answer);
		
		if ((justificationTextNew != null) && (correct)) {
			Justification justification = new Justification(justificationTextNew.toUpperCase(), true,
				userSession.getLoggedUser());
			justification.setValid(true);
			justification.setAnswer(answer);
			justificationService.save(justification);
		}
		for (Question q : answer.getQuestions()) {
			if (!q.isMarked() && (q.getType() == 0)) {
				q.setMarked(true);
				q.setCorrect(correct);
				questionService.save(q);
			}
		}
		return new ResponseEntity<>(answer, HttpStatus.OK);
	}
	
	interface AnswerMarked extends Answer.Marked, Answer.Justifications, Justification.Basic {}
	@JsonView(AnswerMarked.class)
	@GetMapping("/concept/{conceptId}/markedanswers")
	public Page<Answer> getMarked(@PathVariable long conceptId, @PageableDefault(size = DEFAULT_SIZE) Pageable page) {
		return answerService.findByMarkedAndConceptId(true, conceptId, page);
	}
	
	@JsonView(Answer.Basic.class)
	@GetMapping("/concept/{conceptId}/unmarkedanswers")
	public Page<Answer> getUnmarked(@PathVariable long conceptId, @PageableDefault(size = DEFAULT_SIZE) Pageable page) {
		return answerService.findByMarkedAndConceptId(false, conceptId, page);
	}
}
