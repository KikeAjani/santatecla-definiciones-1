package com.group5.definitions.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.group5.definitions.model.Answer;
import com.group5.definitions.model.Justification;
import com.group5.definitions.repositories.JustificationRepository;

@Service
public class JustificationService {
	
	@Autowired
	private JustificationRepository justificationRepository;

	public Justification getRandomJustification(Answer answer) {
		if (answer.isCorrect())
			throw new RuntimeException("Answer is correct and has no justification.");
		long n = justificationRepository.countByAnswer(answer);
		int index = (int) (Math.random() * n);
		Page<Justification> justificationPage = justificationRepository.findByAnswer_Id(answer.getId(), PageRequest.of(index, 1));
		Justification j = null;
		if (justificationPage.hasContent()) {
			j = justificationPage.getContent().get(0);
		}
		return j;
	}

	public void save(Justification justification) {
		justificationRepository.save(justification);
	}

	public Justification findByJustificationText(String justificationText) {
		return justificationRepository.findByJustificationText(justificationText);
	}

	public void deleteById(long id) {
		justificationRepository.deleteById(id);
	}

	public long countByAnswer_Id(long answerId) {
		return justificationRepository.countByAnswer_Id(answerId);
	}

	public Justification findById(long id) {
		return justificationRepository.findById(id).get();
	}

	public Page<Justification> findByAnswer_Id(long answerId, Pageable page) {
		return justificationRepository.findByAnswer_Id(answerId, page);
	}
	
	public Page<Justification> findByAnswer(Answer ans, Pageable page) {
		return justificationRepository.findByAnswer(ans, page);
	}
}
