package com.ozguryazilim.telve.view;

import java.util.Date;

import javax.enterprise.inject.spi.CDI;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import com.ozguryazilim.telve.messages.FacesMessages;
import com.ozguryazilim.telve.messages.FormatedMessage;
import com.ozguryazilim.telve.messages.MessagesUtils;

/**
 * Girilen tarih değerinin bildirilen diğer bileşenlerdeki tarihlerden önce veya
 * sonra gelip gelmediği kontrolünün yapıldığı validator.
 * 
 * @author muhammedf
 *
 */
@FacesValidator("chronologyValidator")
public class ChronologyValidator implements Validator {

	private FormatedMessage formatedMessage;

	private void injectFormatedMessage() {
		formatedMessage = CDI.current().select(FormatedMessage.class).get();
	}

	private Date getDateOf(UIComponent component) {
		if (component == null) {
			return null;
		}
		UIInput input = (UIInput) FacesContext.getCurrentInstance().getViewRoot()
				.findComponent(component.getClientId() + ":" + component.getId() + "_inp");
		return input == null ? null : (Date) input.getValue();
	}

	@Override
	public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {

		if (value != null && value instanceof Date) {
			
			UIComponent currentCompositeComponent = UIComponent.getCurrentCompositeComponent(context);
			UIComponent previousDateComponent = (UIComponent) currentCompositeComponent.getAttributes().get("chronologicalPrevious");
			UIComponent nextDateComponent = (UIComponent) currentCompositeComponent.getAttributes().get("chronologicalNext");

			if (previousDateComponent == null && nextDateComponent == null) {
				return;
			}
			
			Date currentDate = (Date) value;
			injectFormatedMessage();

			String currentComponentLabel = (String) currentCompositeComponent.getAttributes().get("label");
			currentComponentLabel = MessagesUtils.getMessage(currentComponentLabel);

			if (previousDateComponent != null) {
				String previousComponentLabel = (String) previousDateComponent.getAttributes().get("label");
				previousComponentLabel = MessagesUtils.getMessage(previousComponentLabel);
				Date previousDate = getDateOf(previousDateComponent);
				if (previousDate != null && previousDate != null && previousDate.after(currentDate)) {
					FacesMessages.error(formatedMessage.getMessage(MessagesUtils.getMessage("date.message.ThisNotBeforeThat"),
							new Object[] { previousComponentLabel, currentComponentLabel }));
					throw new ValidatorException(new FacesMessage());
				}
			}

			if (nextDateComponent != null) {
				String nextComponentLabel = (String) nextDateComponent.getAttributes().get("label");
				nextComponentLabel = MessagesUtils.getMessage(nextComponentLabel);
				Date nextDate = getDateOf(nextDateComponent);
				if (nextDate != null && nextDate != null && nextDate.before(currentDate)) {
					FacesMessages.error(formatedMessage.getMessage(MessagesUtils.getMessage("date.message.ThisNotAfterThat"),
							new Object[] { nextComponentLabel, currentComponentLabel }));
					throw new ValidatorException(new FacesMessage());
				}
			}

		}

	}

}
