package org.jones.licklibrary.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "chord_shape")
public class ChordShape {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "chord_quality_id")
    private ChordQuality chordQuality;

    private String shapeName;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String templateFrets;

    private int rootString;

    private String source;

    private String label;

    private String instrument;

    public ChordShape() {}

    public UUID getId() { return id; }

    public ChordQuality getChordQuality() { return chordQuality; }
    public void setChordQuality(ChordQuality chordQuality) { this.chordQuality = chordQuality; }

    public String getShapeName() { return shapeName; }
    public void setShapeName(String shapeName) { this.shapeName = shapeName; }

    public String getTemplateFrets() { return templateFrets; }
    public void setTemplateFrets(String templateFrets) { this.templateFrets = templateFrets; }

    public int getRootString() { return rootString; }
    public void setRootString(int rootString) { this.rootString = rootString; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getInstrument() { return instrument; }
    public void setInstrument(String instrument) { this.instrument = instrument; }
}
