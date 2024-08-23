package me.ikevoodoo.helix.builtin.updates;

import me.ikevoodoo.helix.api.types.Some;
import me.ikevoodoo.helix.builtin.commands.repo.update.utils.EntryUpdateData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UpdateList {

    private final List<Some<String, EntryUpdateData>> available;
    private final List<EntryUpdateData> valid;

    public UpdateList(List<Some<String, EntryUpdateData>> available) {
        this.available = Collections.unmodifiableList(available);

        var valid = new ArrayList<EntryUpdateData>();

        for (var entry : available) {
            if (entry.hasValue()) {
                valid.add(entry.value());
            }
        }

        this.valid = Collections.unmodifiableList(valid);
    }

    public List<Some<String, EntryUpdateData>> available() {
        return this.available;
    }

    public List<EntryUpdateData> valid() {
        return this.valid;
    }

    public boolean empty() {
        return this.valid.isEmpty();
    }

    public int availableSize() {
        return this.available.size();
    }

    public int validSize() {
        return this.valid.size();
    }

    public Some<String, EntryUpdateData> available(int index) {
        return this.available.get(index);
    }

    public EntryUpdateData valid(int index) {
        return this.valid.get(index);
    }


}
