package com.softwareengineering.petsitter.ui.user;

import com.softwareengineering.petsitter.pet.dto.PetDto;
import com.softwareengineering.petsitter.ui.shared.PendingImageChange;

public record PetEditorResult(PetDto pet, PendingImageChange imageChange) {
}
