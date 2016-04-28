package edu.virginia.psyc.pi.DAO;

import edu.virginia.psyc.pi.domain.DoNotDelete;
import edu.virginia.psyc.pi.persistence.Questionnaire.QuestionnaireData;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.List;

/**
 * Created by dan on 10/26/15.
 */
@Entity
@Table(name="TestQuestionnaire")
@Data
@EqualsAndHashCode(callSuper=false)
public class TestQuestionnaire extends QuestionnaireData {

    private String value;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "TestMultiValue", joinColumns = @JoinColumn(name = "id"))
    @Column(name = "multiValue")
    private List<String> multiValue;

}
